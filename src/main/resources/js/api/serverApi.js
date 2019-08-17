import Vue from 'vue'

const serverApi = Vue.resource('/channelsDataApi{/channelName}')

export default {
    getChannel: channelName => serverApi.get({channelName: channelName}),
    saveChannel: channel => serverApi.save({}, channel)
}