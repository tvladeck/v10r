class PriceCompute
  require 'gsl'
  include GSL::Sf

  def market_id m_id
    "M#{m_id}"
  end

  def sum m_id, s_id
    exp @redis.hget(market_id(m_id), "SUM").to_f
  end

  def beta m_id, s_id
    @redis.hget(market_id(m_id), "BETA").to_f
  end

  def compute_price_change

  end

end
