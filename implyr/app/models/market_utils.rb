module MarketUtils
  require 'gsl'
  include MarketParams

  def compute_price(position)
    output            = {}
    market_stack      = multi_market_stack position
    signs             = [:buy, :sell]
    SCENARIOS.each do |scen_id, amount|
      output[amount]  = {}
      signs.each do |sign|
        output[amount][sign]  = compute_partial_price sign, scen_id, market_stack
      end
    end
    output
  end

  def compute_partial_price(sign, scen_id, market_stack)
    market_diffs      = market_stack.map do |market_id, stack|
      compute_single_market_change market_id, scen_id, stack[sign]
    end
    market_diffs    = GSL::Vector[market_diffs]
    avg_price_diff  = market_diffs.average
    avg_price_diff
  end

  def compute_single_market_change(market_id, scen_id, stack)
    if stack == [] || stack.nil? then return 0 end
    logmarket_sum   = @redis.hget("M#{market_id}", "SUM").to_f
    market_sum      = GSL::Sf.exp logmarket_sum
    market_beta     = @redis.hget("M#{market_id}", "BETA").to_f
    logdiffs        = @redis.hmget("M#{market_id}S#{scen_id}", *stack).map { |s| s.to_f }
    diffs           = GSL::Sf.exp logdiffs
    diffsum         = diffs.sum
    pricediff       = market_beta * (GSL::Sf.log(market_sum + diffsum) - logmarket_sum)
    pricediff
  end

  def multi_market_stack(position)
    if_part             = position[:if]
    then_part           = position[:then]

    base_events         = []
    if_part.each do |p|
      base_events       = base_events | p[:and] | p[:and_not]
    end
    then_part.each do |p|
      base_events       = base_events | p[:and] | p[:and_not]
    end

    markets             = markets_in_play base_events

    multi_market_stack  = {}
    markets.each do |m|
      multi_market_stack[m] = composition_position_stack(m, position)
    end

    multi_market_stack
  end

  def composition_position_stack(market, composed_position)
    # Args:
    #   market: the integer identifying the relevant market
    #   composed_position: a hash of the form:
    #     { :if => multi_position, :then => multi_position }
    # Returns:
    #   a hash of the form
    #     { :buy => multi_position_stack, :sell => multi_position_stack }
    composed_stack  = {}
    if_part         = composed_position[:if]
    then_part       = composed_position[:then]
    reversed_then   = then_part.map do |partial|
      { :and => partial[:and_not], :and_not => partial[:and] }
    end

    full_stack          = (0..(atoms-1)).to_a
    then_stack          = multi_position_stack(market, then_part) || full_stack
    if_stack            = multi_position_stack(market, if_part) || full_stack
    reversed_then_stack = multi_position_stack(market, reversed_then) || full_stack

    purchase_order  = then_stack & if_stack
    sale_order      = reversed_then_stack & if_stack

    composed_stack[:buy]  = purchase_order
    composed_stack[:sell] = sale_order

    composed_stack
  end

  def multi_position_stack(market, multi_position)
    # takes an array of hashes, each of the form
    # { :and => [events], :and_not => [events] }
    # and returns, for a given market, the matching position stack
    stack = multi_position.map { |entry| partial_position_stack(market, entry) }.
                              reduce { |a, b| a | b }
    stack
  end

  def partial_position_stack(market, partial_position)
    # takes a partial position, which is of the form
    # { :and => [events], :and_not => [events] }
    and_events     = partial_position[:and]
    and_not_events = partial_position[:and_not]
    and_vector_positions(market, and_events) &
      and_not_vector_positions(market, and_not_events)
  end

  def markets_in_play(position)
    # this method returns an array of markets that contain each of the
    # base events given
    markets_in_play = markets.reject do |m_id, base_events|
      !inclusion_test(base_events, position)
    end
    markets_in_play.keys
  end

  def inclusion_test(base_events, position)
    position.map { |p| base_events.include? p }.reduce { |a, b| a && b }
  end

  def and_not_vector_positions(market, position)
    # returns the complement of and_vector_positions
    # returns all vector positions where not all of the base events specified 
    # in the position occur
    full_stack = (0..(atoms-1)).to_a
    if position == [] then return full_stack end

    (0..(atoms-1)).to_a - and_vector_positions(market, position)
  end

  def and_vector_positions(market, position)
    # this method returns the locations w/in the market's atomic event vectors
    # that the position corresponds to within a given market
    # position is an array of base events, with a sign to indicate if the
    # base event is ON or OFF
    full_stack = (0..(atoms-1)).to_a
    if position == [] then return full_stack end

    base_list = markets[market]

    # this returns the position list "normalized" for the base events'
    # placement within the market
    positioning = position.map { |p| GSL::sign(p) * (base_list.find_index(p.abs)) }

    position_map = {}
    positioning.each do |p|
      position_map[p.abs] = if p < 0; 0; else; 1; end
    end

    placement_list = (0..(atoms-1)).select do |i|
      matches_digits(i, position_map)
    end

    placement_list
  end

  def matches_digits(number, digit_map)
    # Integer#[] access the bit representation in ruby
    # this means that 3[2] will tell you what the second bit for the integer
    digit_map.map do |digit, value|
      number[digit] == value
    end.reduce do |a, b|
      a && b
    end
  end
end
