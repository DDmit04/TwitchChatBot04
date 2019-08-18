<template>
    <div class="row justify-content-center">
        <div class="col-4">
            <b-card class="mt-4 shadow">
                <b-card-text>
                    <div class="form-group mt-2">
                        <input class="form-control mb-2"
                               v-model="usernameInput"
                               id="botUsername"
                               placeholder="bot username"
                               name="username"
                               v-validate="'required'"/>
                        <span style="color: red">{{ errors.first('username') }}</span>
                        <input class="form-control mb-2"
                               v-model="oAuthInput"
                               id="botOAuth"
                               placeholder="bot oAuth"
                               name="oAuth"
                               type="password"
                               v-validate="'required'"/>
                        <span style="color: red">{{ errors.first('oAuth') }}</span>
                        <div class="text-center mb-2">
                            <a href="https://twitchapps.com/tmi/">where can I get it?</a>
                        </div>
                        <input class="form-control mb-2"
                               v-model="channelsInput"
                               id="channels"
                               placeholder="channels"
                               name="channels"
                               v-validate="'required'"/>
                        <span style="color: red">{{ errors.first('channels') }}</span>
                        <span style="color: red">{{loadingState.error}}</span>
                        <div class="text-center">
                            <button class="btn btn-primary btn-block my-2"
                                    :disabled="!canStartConnection || loadingState.disconnecting"
                                    @click="loginControl">
                                <div v-if="loadingState.connecting || loadingState.disconnecting">
                                    <span v-if="loadingState.disconnecting">
                                        disconnecting...
                                    </span>
                                    <span v-else>
                                        connecting...
                                    </span>
                                    <b-spinner small></b-spinner>
                                </div>
                                <span v-else-if="bot == null
                                        || loadingState.error != null
                                        || joinedChannels.length == 0">
                                    connect
                                </span>
                                <span v-else>
                                    disconnect
                                </span>
                            </button>
                        </div>
                        <bot-login-connection-notify/>
                    </div>
                </b-card-text>
            </b-card>
        </div>
    </div>
</template>

<script>
    import {mapState, mapMutations, mapActions} from 'vuex'
    import botLoginConnectionNotify from 'components/botLogin/botLoginConnectionNotify.vue'
    import serverApi from 'api/serverApi'


    export default {
        name: "botLogin",
        data() {
            return {
                loadingState: {
                    connecting: false,
                    disconnecting: false,
                    error: null
                }
            }
        },
        components: {
            botLoginConnectionNotify
        },
        created() {
            if(this.isDevMode) {
                this.usernameInput = this.botDevUsername
                this.oAuthInput = this.botDevOauth
                this.channelsInput = this.botDevChannels
            }
        },
        computed: {
            ...mapState(['bot', 'joinedChannels', 'failedJoinChannels', 'currentUser']),
            ...mapState('botDev', ['isDevMode', 'botDevUsername', 'botDevOauth', 'botDevChannels']),
            ...mapState('botLogin', ['username', 'oAuth', 'channels']),
            ...mapState('saveChannelsData', ['savedChannels']),
            usernameInput: {
                get() { return this.username },
                set(newVal) { return this.updateUsernameMutation(newVal) }
            },
            oAuthInput: {
                get() { return this.oAuth },
                set(newVal) { return this.updateOAuthMutation(newVal) }
            },
            channelsInput: {
                get() { return this.channels },
                set(newVal) { return this.updateChannelsMutation(newVal) }
            },
            channelsArray() {
                let channelsArray = []
                this.channelsInput.split(',').forEach(channel => {
                    channelsArray.push(channel.trim())
                })
                return channelsArray
            },
            canStartConnection() {
                let canStart = false
                if(this.usernameInput != null
                    && this.oAuthInput != null
                    && this.channelsInput != null
                    && !this.loadingState.connecting) {
                    canStart = true
                }
                return canStart
            }
        },
        methods: {
            ...mapMutations(['createBotMutation', 'destroyBotMutation','addJoinedChannelMutation', 'addFailedJoinedChannelMutation']),
            ...mapMutations('botLogin', ['updateUsernameMutation', 'updateOAuthMutation', 'updateChannelsMutation']),
            ...mapActions('saveChannelsData', ['pushNewChannelAction', 'addChannelAction']),
            createBot() {
                let options = {
                    options: {
                        debug: this.isDevMode
                    },
                    connection: {
                        secure: true,
                        cluster: 'aws',
                        reconnect: true
                    },
                    identity: {
                        username: this.usernameInput,
                        password: this.oAuthInput
                    },
                }
                this.createBotMutation(options)
            },
            loginControl() {
                if(this.bot === null || this.loadingState.error != null || this.joinedChannels.length == 0) {
                    this.loginBot()
                } else {
                    this.logoutBot()
                }
            },
            async logoutBot() {
                this.loadingState.disconnecting = true
                this.loadingState.connecting = false
                this.loadingState.error = null
                await this.destroyBotMutation()
                if(this.currentUser != null) {
                    await this.saveChannelsToServer()
                }
                this.loadingState.disconnecting = false
            },
            async loginBot() {
                if(this.bot != null && this.loadingState.error == null) {
                    this.destroyBotMutation()
                }
                this.loadingState.error = null
                this.loadingState.connecting = true
                await this.createBot()
                await this.setupBot()
                await this.connectBot()
                await this.loadChannelsFromServer()
                await setTimeout(() => {
                    this.loadingState.connecting = false
                }, this.channelsArray.length * 550)
            },
            setupBot() {
                this.bot.on('connected', this.onConnectedHandler)
                this.bot.on("join", this.onJoinHandler)
                this.bot.on("disconnected", this.onDisconnectedHandler)
                this.bot.on("notice", this.onNoticeHandler)
            },
            async saveChannelsToServer() {
                for (let i = 0; i < this.savedChannels.length; i++) {
                    console.log(this.savedChannels[i])
                    let channel = await serverApi.saveChannel(this.savedChannels[i])
                }
            },
            async loadChannelsFromServer() {
                for (let i = 0; i < this.joinedChannels.length; i++) {
                    let savedChannelSearchInCookies = await this.savedChannels.find(
                        savedChannel => savedChannel.channelName == this.joinedChannels[i]
                    )
                    let serverChannelData = null
                    if(this.currentUser != null) {
                        let savedChannelSearchInServer = await serverApi.getChannel(this.joinedChannels[i])
                        console.log(savedChannelSearchInServer)
                        if(savedChannelSearchInServer.bodyText != '') {
                            serverChannelData = await savedChannelSearchInServer.json()
                        }
                    }
                    if (serverChannelData != null) {
                        this.addChannelAction(serverChannelData)
                    } else if(savedChannelSearchInCookies == null) {
                        console.log(this.joinedChannels[i])
                        console.log(2)
                        this.pushNewChannelAction(this.joinedChannels[i])
                    } else {
                        console.log('lul')
                    }
                }
            },
            async connectBot() {
                await this.bot.connect().catch((err) => { this.loadingState.error = err })
                for(let i = 0; i < this.channelsArray.length; i++) {
                    await this.joinChannel(this.channelsArray[i])
                }
            },
            async joinChannel(channel) {
                await this.bot.join(channel).catch((err) => { this.onJoinErrorHandler(err, channel) })
            },
            onJoinErrorHandler(err, channel) {
                let failedChannelSearch = this.failedJoinChannels.find(
                    failedChannel => failedChannel.channel === channel
                )
                if(failedChannelSearch == null) {
                    this.addFailedJoinedChannelMutation({
                        channel: channel,
                        error: err
                    })
                }
            },
            onJoinHandler(channel, username, self) {
                if(self && this.joinedChannels.length < 5) {
                    this.addJoinedChannelMutation(channel.replace('#', ''))
                }
            },
            onNoticeHandler(channel, msgid, message) {
                console.log('channel: ' + channel + ' msgid: ' + msgid + ' message: ' + message)
            },
            onConnectedHandler(address, port) {
                console.log('Joined channel: ' + address + ' port: ' + port)
            },
            onDisconnectedHandler(reason) {
                console.log('disconnected channel: ' + reason)
            },
        }
    }
</script>

<style scoped>

</style>