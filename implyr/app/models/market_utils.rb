module MarketUtils
  require 'redis'
  require 'gsl'
  include MarketParams


  def partial_position_stack(market, partial_position)
    # takes a full position, which is of the form
    # { :and => [events], :and_not => [events] }
    # and returns a hash of the form
    # { :market => :positions }
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
    (0..(atoms-1)).to_a - and_vector_positions(market, position)
  end

  def and_vector_positions(market, position)
    # this method returns the locations w/in the market's atomic event vectors
    # that the position corresponds to within a given market
    # position is an array of base events, with a sign to indicate if the
    # base event is ON or OFF
    base_list = markets[market]

    # this returns the position list "normalized" for the base events'
    # placement within the market
    positioning = position.map { |p| (p <=> 0) * (base_list.find_index(p.abs)) }

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
