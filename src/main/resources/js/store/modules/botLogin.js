const state = {
    username: '',
    oAuth: '',
    channels: '',
}
const getters = {}
const mutations = {
    updateUsernameMutation(state, username) {
        state.username = username
    },
    updateOAuthMutation(state, oAuth) {
        state.oAuth = oAuth
    },
    updateChannelsMutation(state, channelsInput) {
        state.channels = channelsInput
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