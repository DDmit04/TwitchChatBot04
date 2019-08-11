const state = {
    savedChannels: [],
}
const getters = {}
const mutations = {
    pushNewChannelMutation(state, newChannel) {
        state.savedChannels.push(newChannel)
    },
    pushNewMessageVoteInstanceMutation(state, {instance, channel}) {
        let savedChannelSearch = state.savedChannels.find(savedChannel => savedChannel.channelName == channel)
        savedChannelSearch.messageVoteInstance = Object.assign({}, instance)
    },
    updateMessageVoteInstanceMutation(state, {updatedInstance, channel}) {
        let savedChannelSearch = state.savedChannels.find(savedChannel => savedChannel.channelName == channel)
        savedChannelSearch.messageVoteInstance = Object.assign({}, updatedInstance)
    },
    pushNewMessageCountInstanceMutation(state, {instance, channel}) {
        let savedChannelSearch = state.savedChannels.find(savedChannel => savedChannel.channelName == channel)
        savedChannelSearch.messageCountInstance = Object.assign({}, instance)
    },
    updateMessageCountInstanceMutation(state, {updatedInstance, channel}) {
        let savedChannelSearch = state.savedChannels.find(savedChannel => savedChannel.channelName == channel)
        savedChannelSearch.messageCountInstance = Object.assign({}, updatedInstance)
    },
    pushNewCollingCommandInstanceMutation(state, {instance, channel}) {
        let savedChannelSearch = state.savedChannels.find(savedChannel => savedChannel.channelName == channel)
        savedChannelSearch.callingCommandInstance = Object.assign({}, instance)
    },
    updateCollingCommandInstanceMutation(state, {updatedInstance, channel}) {
        let savedChannelSearch = state.savedChannels.find(savedChannel => savedChannel.channelName == channel)
        savedChannelSearch.callingCommandInstance = Object.assign({}, updatedInstance)
    },
    pushNewRegularCommandInstanceMutation(state, {instance, channel}) {
        let savedChannelSearch = state.savedChannels.find(savedChannel => savedChannel.channelName == channel)
        savedChannelSearch.regularCommandInstance = Object.assign({}, instance)
    },
    updateRegularCommandInstanceMutation(state, {updatedInstance, channel}) {
        let savedChannelSearch = state.savedChannels.find(savedChannel => savedChannel.channelName == channel)
        savedChannelSearch.regularCommandInstance = Object.assign({}, updatedInstance)
    },
}
const actions = {
    pushNewChannelAction({commit}, channel) {
        let newChannel = {
            channelName: channel,
            regularCommandInstance: null,
            callingCommandInstance: null,
            messageCountInstance: null,
            messageVoteInstance: null
        }
      commit('pushNewChannelMutation', newChannel)
    },
    pushNewMessageVoteInstanceAction({commit}, {instance, channel}) {
        let pushedInstance = {
            instance: instance,
            channel: channel
        }
        commit('pushNewMessageVoteInstanceMutation', pushedInstance)
    },
    updateMessageVoteInstanceAction({commit}, {updatedInstance, channel}) {
        let updatedInst = {
            updatedInstance: updatedInstance,
            channel: channel
        }
        commit('updateMessageVoteInstanceMutation', updatedInst)
    },
    pushNewMessageCountInstanceAction({commit}, {instance, channel}) {
        let pushedInstance = {
            instance: instance,
            channel: channel
        }
        commit('pushNewMessageCountInstanceMutation', pushedInstance)
    },
    updateMessageCountInstanceAction({commit}, {updatedInstance, channel}) {
        let updatedInst = {
            updatedInstance: updatedInstance,
            channel: channel
        }
        commit('updateMessageCountInstanceMutation', updatedInst)
    },
    pushNewCollingCommandInstanceAction({commit}, {instance, channel}) {
        let pushedInstance = {
            instance: instance,
            channel: channel
        }
        commit('pushNewCollingCommandInstanceMutation', pushedInstance)
    },
    updateCollingCommandInstanceAction({commit}, {updatedInstance, channel}) {
        let updatedInst = {
            updatedInstance: updatedInstance,
            channel: channel
        }
        commit('updateCollingCommandInstanceMutation', updatedInst)
    },
    pushNewRegularCommandInstanceAction({commit}, {instance, channel}) {
        let pushedInstance = {
            instance: instance,
            channel: channel
        }
        commit('pushNewRegularCommandInstanceMutation', pushedInstance)
    },
    updateRegularCommandInstanceAction({commit}, {updatedInstance, channel}) {
        let updatedInst = {
            updatedInstance: updatedInstance,
            channel: channel
        }
        commit('updateRegularCommandInstanceMutation', updatedInst)
    },
}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations
}