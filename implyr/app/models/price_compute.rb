class PriceCompute
  require 'gsl'
  include MarketUtils
  include MarketParams
  include GSL::Sf

  def initialize(array)
    @markets = MarketParams.markets_in_play(array)
  end

  def compute_price_change

  end

end
