import Vue from 'vue'

const twitchApi = Vue.resource('https://cors-anywhere.herokuapp.com/https://tmi.twitch.tv/group/user{/chanel}/chatters')

export default {
    getViewers: chanel => twitchApi.get({chanel: chanel})
}