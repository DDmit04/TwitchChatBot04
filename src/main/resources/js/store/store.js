import Vue from 'vue'
import Vuex from 'vuex'
import createPersistedState from 'vuex-persistedstate'
import * as Cookie from 'js-cookie'
import channelsNavbar from 'store/modules/channelsNavbar'
import botDev from 'store/modules/botDev'
import saveChannelsData from 'store/modules/saveChannelsData'
import botLogin from 'store/modules/botLogin'
import localInstances from 'store/modules/localInstances'

Vue.use(Vuex)

export default new Vuex.Store({
    strict: botDev.state.isDevMode,
    state: {
        bot: null,
        joinedChannels: [],
        failedJoinChannels: [],
    },
    plugins: [
        createPersistedState({
            paths: ['saveChannelsData', 'botLogin'],
            getState: (key) => Cookie.getJSON(key),
            setState: (key, state) => Cookie.set(key, state, { expires: 1, secure: false })
        })
    ],
    modules: {
        channelsNavbar,
        botDev,
        saveChannelsData,
        botLogin,
        localInstances
    },
    getters: {},
    mutations: {
        createBotMutation(state, options) {
            let tmi = require('tmi.js')
            state.bot = new tmi.client(options)
            state.joinedChannels = []
            state.failedJoinChannels = []
        },
        destroyBotMutation(state) {
            state.bot.disconnect()
            state.bot = null
            localInstances.mutations.cleanModuleDataMutation(localInstances.state)
            channelsNavbar.mutations.cleanModuleDataMutation(channelsNavbar.state)
            channelsNavbar.state.lastSelectedChanelVote = null
            channelsNavbar.state.lastSelectedChanelCommands = null
        },
        addJoinedChannelMutation(state, channel) {
            state.joinedChannels.push(channel)
        },
        addFailedJoinedChannelMutation(state, joinErrorState) {
            state.failedJoinChannels.push(joinErrorState)
        },
    },
    actions: {

    }
})