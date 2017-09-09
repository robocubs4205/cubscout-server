var webpack = require('webpack');

module.exports = require('./scalajs.webpack.config.js');

Object.keys(module.exports.entry).forEach(function(key){
    module.exports.entry[key] = ['./mui.js'].concat(module.exports.entry[key])
});