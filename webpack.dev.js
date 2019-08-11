const merge = require('webpack-merge');
const common = require('./webpack.common.js');

module.exports = merge(common, {
    mode: 'development',
    devtool: 'source-map',
    devServer: {
        contentBase: './dist',
        compress: true,
        port: 8003,
        allowedHosts: [
            'localhost:9003'
        ],
        stats: 'minimal',
        clientLogLevel: 'error',
    },
});