#!/usr/bin/env ruby


require 'nakamura/test'
require 'logger'

SlingTest.setLogLevel(Logger::ERROR)

Dir.foreach(".") do |path|
  if /kern-.*\.rb/.match(File.basename(path))
    require "./#{path}"
  end
end
