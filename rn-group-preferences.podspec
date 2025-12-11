require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = 'rn-group-preferences'
  s.version      = package['version']
  s.summary      = package['description']
  s.homepage     = package['homepage']
  s.license      = package['license']
  s.author       = { "Muhamad Rizki" => "github: byrizki" }
  s.platform     = :ios, "11.0"
  s.source       = { :git => "https://github.com/byrizki/rn-group-preferences.git", :branch => "master", :tag => "v#{s.version}" }
  s.source_files = "ios/*.{h,mm}"
  s.requires_arc = true

  s.dependency "React-Core"
  
  # New Architecture
  install_modules_dependencies(s)
end
