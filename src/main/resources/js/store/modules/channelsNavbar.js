const state = {
    lastSelectedChanelCommands: null,
    lastSelectedChanelVote: null
}
const getters = {}
const mutations = {
    updateSelectedChanelCommandsMutation(state, channel) {
        state.lastSelectedChanelCommands = channel
    },
    updateSelectedChanelVoteMutation(state, channel) {
        state.lastSelectedChanelVote = channel
    }
}
const actions = {}

export default {
    namespaced: true,
    state,
    getters,
    actions,
    mutations
}