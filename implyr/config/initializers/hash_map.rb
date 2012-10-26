class Array
  def hash_map
    h = {}
    self.each do |e|
      h[e] = yield(e)
    end
    return(h)
  end
end
