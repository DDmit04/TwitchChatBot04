const state = {
    messageCountInstances: [],
    messageVoteInstances: [],
    regularCommandInstances: [],
    collingCommandInstances: [],
}
const getters = {}
const mutations = {
    pushNewLocalMessageCountInstanceMutation(state, channel) {
        state.messageCountInstances.push(channel)
    },
    pushNewLocalMessageVoteInstanceMutation(state, channel) {
        state.messageVoteInstances.push(channel)
    },
    pushNewLocalRegularCommandInstanceMutation(state, channel) {
        state.regularCommandInstances.push(channel)
    },
    pushNewLocalCollingCommandInstanceMutation(state , channel) {
        state.collingCommandInstances.push(channel)
    },
    cleanModuleDataMutation(state) {
        state.messageCountInstances = []
        state.messageVoteInstances = []
        state.regularCommandInstances = []
        state.collingCommandInstances = []
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