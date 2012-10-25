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


end
