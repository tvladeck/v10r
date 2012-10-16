module MarketParams
  require 'algorithms'
  extend self

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


end
