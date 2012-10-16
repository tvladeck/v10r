class PriceCompute
  require 'gsl'
  include MarketUtils
  include MarketParams
  include GSL::Sf

  def initialize(position)
    @markets = markets_in_play(position)
    @position = position
  end

  def compute_price_change
    
  end

end
