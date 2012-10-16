module MarketParams
  require 'algorithms'
  extend self

  def base
    10
  end

  def atoms
    2 ** base
  end

  def slack
    0.1
  end

  def alpha
    slack / (atoms * Math.log(atoms))
  end

  def markets
    # this is a hash representing which markets contain which events
    # each base event has a numeric identifier
    # each market has a numeric identifier
    { 1 => [0, 1, 2, 3, 4, 5, 6, 7, 8, 9], 
      2 => [5, 6, 7, 8, 9, 10, 11, 12, 13, 14] }
  end

  def markets_in_play(position)
    # this method returns an array of markets that contain each of the
    # base events given
    markets = self.markets
    markets_in_play = markets.reject do |m_id, base_events|
      !self.inclusion_test(base_events, position)
    end
    markets_in_play.keys
  end

  def inclusion_test(base_events, position)
    position.map { |p| base_events.include? p }.reduce { |a, b| a && b }
  end

  def vector_positions(market, position)
    # this method returns the locations w/in the market's atomic event vectors
    # that the position corresponds to within a given market
    # position is an array of base events, with a sign to indicate if the
    # base event is ON or OFF
    base_list = self.markets[market]

    # this returns the position list "normalized" for the base events'
    # placement within the market
    positioning = position.map { |p| (p <=> 0) * (base_list.find_index(p.abs)) }

    position_map = {}
    positioning.each do |p|
      position_map[p.abs] = if p < 0; 0; else; 1; end
    end

    placement_list = (0..(self.atoms-1)).select do |i|
      self.matches_digits(i, position_map)
    end

    placement_list
  end

  def digiter(number, digit)
    # Args:
    #   number: any integer
    #   digit: the digit of that integer you want to test
    # Returns:
    #   the digit of the binary representation of number
    if (number / (2 ** digit)).floor.even?
      0
    else
      1
    end
  end

  def matches_digits(number, digit_map)
    digit_map.map do |digit, value|
      self.digiter(number, digit) == value
    end.reduce do |a, b|
      a && b
    end
  end

end
