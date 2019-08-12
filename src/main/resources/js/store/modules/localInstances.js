const state = {
    messageCountInstances: [],
    messageVoteInstances: [],
    regularCommandInstances: [],
    collingCommandInstances: [],
}
const getters = {}
const mutations = {
    pushNewMessageCountInstancesInstance(state, channel) {
        state.messageCountInstances.push(channel)
    },
    pushNewMessageVoteInstancesInstance(state, channel) {
        state.messageVoteInstances.push(channel)
    },
    pushNewRegularCommandInstance(state, channel) {
        state.regularCommandInstances.push(channel)
    },
    pushNewCollingCommandInstance(state ,channel) {
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