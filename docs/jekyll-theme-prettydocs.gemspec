# coding: utf-8

Gem::Specification.new do |spec|
  spec.name          = "jekyll-theme-prettydocs"
  spec.version       = "0.0.2"
  spec.authors       = ["Maicon Pinto"]
  spec.email         = ["maiconsilva.pinto@gmail.com"]

  spec.summary       = "Jekyll Theme PrettyDocs"
  spec.homepage      = "http://maiconpinto.com.br"
  spec.license       = "MIT"

  spec.files         = `git ls-files -z`.split("\x0").select { |f| f.match(%r{^(assets|_layouts|_includes|_sass|LICENSE|README)}i) }

  spec.add_development_dependency "jekyll", "~> 3.3"
  spec.add_development_dependency "bundler", "~> 1.12"
  spec.add_development_dependency "rake", "~> 10.0"
end
