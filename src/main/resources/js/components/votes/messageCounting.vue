<template>
    <div>
        <b-card class="shadow">
            <div slot="header">
                <b-progress v-if="messageCount > 0 || votingIsRunning"
                            class="mb-2"
                            :max="progressMax"
                            height="2rem">
                        <b-progress-bar v-for="(messageCountObject, index) in messageCountObjects"
                                        :key="index"
                                        :value="(messageCountObject.count / messageCount) * progressMax"
                                        :variant="bars[index]"
                                        :animated="votingIsRunning">
                            <div v-if="!isNaN(messageCountObject.count / messageCount)">
                                {{messageCountObject.message}}
                                {{((messageCountObject.count / messageCount) * progressMax).toFixed(0)}}%
                            </div>
                            <div v-else>
                                {{messageCountObject.message}}
                            </div>
                        </b-progress-bar>
                </b-progress>
                <ul class="list-group">
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        message count:
                        <span class="badge badge-primary badge-pill">{{messageCount}}</span>
                    </li>
                </ul>
            </div>
            <b-card-text>
                <div class="form-group">
                    <input class="form-control mb-2"
                           type="text"
                           :disabled="votingIsRunning"
                           v-model="messagesToCountInput"
                           name="messages to count"
                           placeholder="messages to count"
                           v-validate="'required'"/>
                    <span style="color: red">{{ errors.first('messages to count') }}</span>
                    <input class="form-control mb-2"
                           type="number"
                           :disabled="votingIsRunning"
                           v-model="voteTimeInput"
                           placeholder="vote time (sec)"
                           name="count time"
                           v-validate="{ min_value: 0 }"/>
                    <span style="color: red">{{ errors.first('count time') }}</span>
                    <b-button class="btn-block mb-2"
                              v-b-toggle.collapse
                              variant="primary">
                        more options
                    </b-button>
                    <b-collapse class="mb-2" id="collapse">
                        <ul class="list-group">
                            <li class="list-group-item">
                                <b-form-checkbox v-model="ignoreLetterCase"
                                                 :disabled="votingIsRunning"
                                                 switch>
                                    ignore case
                                </b-form-checkbox>
                            </li>
                            <li class="list-group-item">
                                <b-form-checkbox v-model="countUserOnce"
                                                 :disabled="votingIsRunning"
                                                 switch>
                                    one counted message per user
                                </b-form-checkbox>
                            </li>
                        </ul>
                    </b-collapse>
                </div>
                <button class="btn btn-primary"
                        :disabled="!voteCanStart"
                        @click="voteControl">
                    <span v-if="votingIsRunning">
                        stop counting
                        <span class="badge badge-light">
                        {{getVoteTime()}}
                    </span>
                    </span>
                    <span v-else>
                        start counting
                    </span>
                </button>
            </b-card-text>
        </b-card>
    </div>
</template>

<script>
    import {mapState, mapMutations, mapActions} from 'vuex'
    import {convertSecToTime} from 'helpers/convertSecToTime'
    import _ from 'lodash'

    export default {
        name: "messageCounting",
        props: ['channel'],
        data() {
            return {
                bars: [
                    'success',
                    'warning',
                    'danger',
                    'primary',
                ],
                progressMax: 100,
                localInstances: [],
                defaultLocalInstance: {
                    channel: null,
                    messageCountObjects: [],
                    messageCount: 0,
                    voteTime: 0,
                    votingIsRunning: false,
                    countedUsers: [],
                    active: true
                },
                defaultGlobalInstance: {
                    messagesToCountInput: 'SMOrc',
                    voteTimeInput: 60,
                    countUserOnce: false,
                    ignoreLetterCase: false
                }
            }
        },
        created() {
            if(this.messageCountInstances.length == 0) {
                for (let i = 0; i < this.joinedChannels.length; i++) {
                    let savedChannelsSearch = this.savedChannels.find(channel => channel.channelName == this.joinedChannels[i])
                    let globalMessageCountInstance
                    if (savedChannelsSearch.messageCountInstance == null) {
                        globalMessageCountInstance = {...this.defaultGlobalInstance}
                        this.pushNewMessageCountInstanceAction({
                            instance: globalMessageCountInstance,
                            channel: this.joinedChannels[i]
                        })
                    } else {
                        globalMessageCountInstance = {...savedChannelsSearch.messageCountInstance}
                    }
                    let newLocalInstance = {...this.defaultLocalInstance, ...globalMessageCountInstance}
                    newLocalInstance.channel = this.joinedChannels[i]
                    this.localInstances.push(newLocalInstance)
                    this.pushNewMessageCountInstancesInstance(newLocalInstance.channel)
                    this.bot.on('message', (target, context, message, self) => {
                        this.onMessageHandler(target, context, message, self, newLocalInstance)
                    })
                }
            }
        },
        computed: {
            ...mapState(['bot', 'joinedChannels', 'savedChannels']),
            ...mapState('saveChannelsData', ['savedChannels']),
            ...mapState('localInstances', ['messageCountInstances']),
            currentChannel() { return this.currentLocalVoteInstance.channel },
            voteCanStart() {
                let canStart = false
                if(
                    this.messagesToCountInput != null
                    && (this.voteTimeInput > 0 || this.voteTimeInput == '')) {
                    canStart = true
                }
                return canStart
            },
            currentLocalVoteInstance() {
                let voteInstanceSearch = this.localInstances.find(inst => inst.channel == this.channel)
                let instance = null
                if(voteInstanceSearch == null) {
                    instance = {...this.defaultGlobalInstance}
                } else {
                    instance = voteInstanceSearch
                }
                return instance
            },
            updatedGlobalInstance() {
                return {
                    messagesToCountInput: this.messagesToCountInput,
                    voteTimeInput: this.voteTimeInput,
                    countUserOnce: this.countUserOnce,
                    ignoreLetterCase: this.ignoreLetterCase
                }
            },
            messagesToCount() {
                let messageArray = []
                this.messagesToCountInput.split(',').forEach(message => {
                    if(messageArray.length < this.bars.length) {
                        messageArray.push(message.trim())
                    }
                })
                return messageArray
            },
            messagesToCountInput: {
                get() { return this.currentLocalVoteInstance.messagesToCountInput },
                set(newVal) {
                    this.currentLocalVoteInstance.messagesToCountInput = newVal
                    this.updateGlobalInstance()
                }
            },
            ignoreLetterCase: {
                get() { return this.currentLocalVoteInstance.ignoreLetterCase },
                set(newVal) {
                    this.currentLocalVoteInstance.ignoreLetterCase = newVal
                    this.updateGlobalInstance()
                }
            },
            countUserOnce: {
                get() { return this.currentLocalVoteInstance.countUserOnce },
                set(newVal) {
                    this.currentLocalVoteInstance.countUserOnce = newVal
                    this.updateGlobalInstance()
                }
            },
            voteTimeInput: {
                get() { return this.currentLocalVoteInstance.voteTimeInput },
                set(newVal) {
                    this.currentLocalVoteInstance.voteTimeInput = newVal
                    this.updateGlobalInstance()
                }
            },
            countedUsers: {
                get() { return this.currentLocalVoteInstance.countedUsers },
                set(newVal) { this.currentLocalVoteInstance.countedUsers = newVal }
            },
            voteTime: {
                get() { return this.currentLocalVoteInstance.voteTime },
                set(newVal) { this.currentLocalVoteInstance.voteTime = newVal }
            },
            votingIsRunning: {
                get() { return this.currentLocalVoteInstance.votingIsRunning },
                set(newVal) { this.currentLocalVoteInstance.votingIsRunning = newVal }
            },
            messageCountObjects: {
                get() { return this.currentLocalVoteInstance.messageCountObjects },
                set(newVal) { this.currentLocalVoteInstance.messageCountObjects = newVal }
            },
            messageCount: {
                get() { return this.currentLocalVoteInstance.messageCount },
                set(newVal) { this.currentLocalVoteInstance.messageCount = newVal }
            },
            voteTimeMeter: {
                get() { return this.currentLocalVoteInstance.voteTimeMeter },
                set(newVal) { this.currentLocalVoteInstance.voteTimeMeter = newVal }
            },
            voteTimer: {
                get() { return this.currentLocalVoteInstance.voteTimer },
                set(newVal) { this.currentLocalVoteInstance.voteTimer = newVal }
            },
        },
        methods: {
            ...mapActions('saveChannelsData', ['pushNewMessageCountInstanceAction', 'updateMessageCountInstanceAction', 'pushNewChannelMutation']),
            ...mapMutations('localInstances', ['pushNewMessageCountInstancesInstance', 'cleanMessageCountInstancesInstances']),
            updateGlobalInstance() {
                this.updateMessageCountInstanceAction({
                    updatedInstance: this.updatedGlobalInstance,
                    channel: this.currentChannel
                })
            },
            voteControl() {
                if(this.votingIsRunning) {
                    this.stopVoting()
                } else {
                    this.startVoting()
                }
            },
            getVoteTime() {
              return convertSecToTime(this.voteTime)
            },
            setupMessageCountObjects() {
                this.messageCountObjects = []
                for (let i = 0; i < this.messagesToCount.length; i++) {
                    let messageCountObject = {
                        message: this.messagesToCount[i],
                        count: 0
                    }
                    this.messageCountObjects.push(messageCountObject)
                }
            },
            setupNewVoteInstance() {
                this.voteTimer = null
                this.voteTimeMeter = null
                this.messageCount = 0
                this.voteTime = this.voteTimeInput
            },
            startVoting() {
                this.votingIsRunning = true
                this.setupMessageCountObjects()
                this.setupNewVoteInstance()
                let chatVoteTime
                if(this.voteTime <= 0) {
                    chatVoteTime = 'infinity'
                } else {
                    chatVoteTime = this.voteTimeInput
                }
                this.bot.action(this.currentChannel,
                    'counting is started for ' + chatVoteTime + ' sec! messages: '
                    + this.messagesToCount
                )
                if(this.voteTime <= 0) {
                    this.voteTime = Infinity
                } else {
                    this.startVoteTimers()
                }
            },
            startVoteTimers() {
                this.voteTimeMeter = setInterval(() => {
                    if(this.voteTime > 0) {
                        this.voteTime--
                    } else {
                        clearInterval(this.voteTimeMeter)
                    }
                }, 1000)
                this.voteTimer = setTimeout(() => {
                    this.stopVoting()
                }, this.voteTimeInput * 1000)
            },
            stopVoting() {
                clearInterval(this.voteTimeMeter)
                clearInterval(this.voteTimer)
                if(this.voteTime != 0) {
                    this.voteTime = 0
                }
                this.votingIsRunning = false
                this.countedUsers = []
                this.bot.action(this.currentChannel,
                    'counting ended, messages: ' + this.messageCount)
            },
            onMessageHandler(target, context, message, self, currentLocalVoteInstance) {
                if(self) {
                    return
                }
                let userAlreadyVote = false
                if(this.countUserOnce) {
                    let votedUserSearch = currentLocalVoteInstance.countedUsers.find(user => user === context.username)
                    userAlreadyVote = (votedUserSearch != null)
                }
                let messagesToSearch = this.messagesToCount
                let searchingMessage = message
                if(this.ignoreLetterCase) {
                    searchingMessage = message.toLowerCase()
                    messagesToSearch = this.messagesToCount.toString().toLowerCase().split(',')
                }
                if (!userAlreadyVote
                    && currentLocalVoteInstance.votingIsRunning
                    && target.replace('#','') === currentLocalVoteInstance.channel) {

                    let pos = -1
                    for (let i = 0; i < messagesToSearch.length; i++) {
                        while ((pos = searchingMessage.indexOf(messagesToSearch[i], pos + 1)) != -1) {
                            let messageCountObjectSearch = this.messageCountObjects.find(
                                messageCountObject => {
                                    let messageCountObjectRes
                                    if(this.ignoreLetterCase) {
                                        messageCountObjectRes = messageCountObject.message.toLowerCase() == messagesToSearch[i]
                                    } else {
                                        messageCountObjectRes = messageCountObject.message == messagesToSearch[i]
                                    }
                                    return messageCountObjectRes
                                }
                            )
                            messageCountObjectSearch.count++
                            currentLocalVoteInstance.messageCount++
                        }
                    }
                    if(this.countUserOnce) {
                        currentLocalVoteInstance.countedUsers.push(context.username)
                    }
                }
            }
        }
    }
</script>

<style scoped>

</style>