<template>
    <div>
        <b-card class="shadow">
            <div slot="header">
                <b-progress v-if="voteResult != 0 || votingIsRunning"
                        class="mb-2"
                            :max="Number(upBorder)"
                            :animated="votingIsRunning"
                            height="2rem">
                    <b-progress-bar :value="voteResult">
                            <div v-if="voteResult != 0">
                        {{voteResult}}/{{upBorder}}
                        </div>
                    </b-progress-bar>
                </b-progress>
                <ul class="list-group">
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        votes:
                        <span class="badge badge-primary badge-pill">{{messageCount}}</span>
                    </li>
                    <li class="list-group-item d-flex justify-content-between align-items-center">
                        result:
                        <span class="badge badge-primary badge-pill">{{voteResult}}</span>
                    </li>
                </ul>
            </div>
            <b-card-text>
                <div class="form-group">
                    <input class="form-control mb-2"
                           type="number"
                           :disabled="votingIsRunning"
                           v-model="downBorder"
                           name="down border"
                           placeholder="down border"
                           v-validate="'required'"/>
                    <span style="color: red">{{ errors.first('down border') }}</span>
                    <input class="form-control mb-2"
                           type="number"
                           :disabled="votingIsRunning"
                           v-model="upBorder"
                           name="up border"
                           placeholder="up border"
                           v-validate="'required'"/>
                    <span style="color: red">{{ errors.first('up border') }}</span>
                    <input class="form-control mb-2"
                           type="number"
                           :disabled="votingIsRunning"
                           v-model="currentLocalVoteInstance.voteTimeInput"
                           placeholder="vote time (sec)"
                           name="vote time"
                           v-validate="{ min_value: 0 }"/>
                    <span style="color: red">{{ errors.first('vote time') }}</span>
                </div>
                <div v-if="Number(downBorder) >= Number(upBorder)
                        && Number(upBorder) <= Number(downBorder)"
                     style="color: red">
                    borders error
                </div>
                <button class="btn btn-primary"
                        :disabled="!voteCanStart"
                        @click="voteControl">
                    <div v-if="votingIsRunning">
                        stop voting
                        <span class="badge badge-light">
                        {{getVoteTime()}}
                    </span>
                    </div>
                    <div v-else>
                        start voting
                    </div>
                </button>
            </b-card-text>
        </b-card>

        </div>
</template>

<script>
    import {mapState, mapActions} from 'vuex'
    import {convertSecToTime} from 'helpers/convertSecToTime'
    import _ from 'lodash'

    export default {
        name: "userVoting",
        props: ['channel'],
        data() {
            return {
                localInstances: [],
                //global = store (saved)
                defaultGlobalInstance: {
                    downBorder: 1,
                    upBorder: 10,
                    voteTimeInput: 60,
                },
                //local = component (not saved)
                defaultLocalInstance: {
                    channel: null,
                    voteTime: 0,
                    votingIsRunning: false,
                    countedUsers: [],
                    messageCount: 0,
                    messageSum: 0,
                    voteResult: 0,
                    voteTimer: null,
                    voteTimeMeter: null,
                }
            }
        },
        created() {
            for (let i = 0; i < this.joinedChannels.length; i++) {
                let savedChannelsSearch = this.savedChannels.find(channel => channel.channelName == this.joinedChannels[i])
                let globalMessageVoteInstance
                if (savedChannelsSearch.messageVoteInstance == null) {
                    globalMessageVoteInstance = {...this.defaultGlobalInstance}
                    this.pushNewMessageVoteInstanceAction({
                        instance: globalMessageVoteInstance,
                        channel: this.joinedChannels[i]
                    })
                } else {
                    globalMessageVoteInstance = savedChannelsSearch.messageVoteInstance
                }
                let newLocalInstance = {...this.defaultLocalInstance, ...globalMessageVoteInstance}
                newLocalInstance.channel = this.joinedChannels[i]
                this.localInstances.push(newLocalInstance)
                this.bot.on('message', (target, context, message, self) => {
                    this.onMessageHandler(target, context, message, self, newLocalInstance)
                })
            }
        },
        computed: {
            ...mapState(['bot', 'joinedChannels']),
            ...mapState('saveChannelsData', ['savedChannels']),
            currentChannel() { return this.currentLocalVoteInstance.channel },
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
                    downBorder: this.downBorder,
                    upBorder: this.upBorder,
                    voteTimeInput: this.voteTimeInput,
                }
            },
            voteCanStart() {
                let canStart = false
                if(
                    Number(this.downBorder) < Number(this.upBorder)
                    && Number(this.upBorder) > Number(this.downBorder)
                    && this.upBorder != null
                    && this.downBorder != null
                    && (this.voteTimeInput > 0 || this.voteTimeInput == '')) {
                    canStart = true
                }
                return canStart
            },
            downBorder: {
                get() { return this.currentLocalVoteInstance.downBorder },
                set(newVal) {
                    this.currentLocalVoteInstance.downBorder = newVal
                    this.updateGlobalInstance()
                }
            },
            upBorder: {
                get() { return this.currentLocalVoteInstance.upBorder },
                set(newVal) {
                    this.currentLocalVoteInstance.upBorder = newVal
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
            votingIsRunning: {
                get() { return this.currentLocalVoteInstance.votingIsRunning },
                set(newVal) { this.currentLocalVoteInstance.votingIsRunning = newVal }
            },
            messageCount: {
                get() { return this.currentLocalVoteInstance.messageCount },
                set(newVal) { this.currentLocalVoteInstance.messageCount = newVal }
            },
            voteResult: {
                get() { return this.currentLocalVoteInstance.voteResult },
                set(newVal) { this.currentLocalVoteInstance.voteResult = newVal }
            },
            messageSum: {
                get() { return this.currentLocalVoteInstance.messageSum },
                set(newVal) { this.currentLocalVoteInstance.messageSum = newVal }
            },
            voteTimer: {
                get() { return this.currentLocalVoteInstance.voteTimer },
                set(newVal) { this.currentLocalVoteInstance.voteTimer = newVal }
            },
            voteTimeMeter: {
                get() { return this.currentLocalVoteInstance.voteTimeMeter },
                set(newVal) { this.currentLocalVoteInstance.voteTimeMeter = newVal }
            },
            voteTime: {
                get() { return this.currentLocalVoteInstance.voteTime },
                set(newVal) { this.currentLocalVoteInstance.voteTime = newVal }
            },
            countedUsers: {
                get() { return this.currentLocalVoteInstance.countedUsers },
                set(newVal) { this.currentLocalVoteInstance.countedUsers = newVal }
            },
        },
        methods: {
            ...mapActions('saveChannelsData', ['pushNewMessageVoteInstanceAction', 'updateMessageVoteInstanceAction']),
            updateGlobalInstance() {
                this.updateMessageVoteInstanceAction({
                    updatedInstance: this.updatedGlobalInstance,
                    channel: this.currentChannel
                })
            },
            voteControl() {
                if(this.votingIsRunning) {
                    this.stopVoting(this.currentChannel)
                } else {
                    this.startVoting(this.currentChannel)
                }
            },
            getVoteTime() {
                return convertSecToTime(this.voteTime)
            },
            setupNewVoteInstance() {
                this.messageCount = 0
                this.messageSum = 0
                this.voteResult = 0
                this.voteTimer = null
                this.voteTimeMeter = null
                this.voteTime = this.voteTimeInput
            },
            startVoting(channel) {
                this.votingIsRunning = true
                this.setupNewVoteInstance()
                let chatVoteTime
                if(this.voteTime <= 0) {
                    chatVoteTime = 'infinity'
                } else {
                    chatVoteTime = this.voteTimeInput
                }
                this.bot.action(channel,
                    'voting is started for ' + chatVoteTime + ' sec! from: '
                    + this.downBorder + ' to: ' + this.upBorder
                )
                if(this.voteTime <= 0) {
                    this.voteTime = Infinity
                } else {
                    this.startVoteTimers(channel)
                }
            },
            startVoteTimers(channel) {
                this.voteTimeMeter = setInterval(() => {
                    if(this.voteTime > 0) {
                        this.voteTime--
                    } else {
                        clearInterval(this.voteTimeMeter)
                    }
                }, 1000)
                this.voteTimer = setTimeout(() => {
                    this.stopVoting(channel)
                }, this.voteTimeInput * 1000)
            },
            stopVoting(channel) {
                clearInterval(this.voteTimeMeter)
                clearInterval(this.voteTimer)
                if(this.voteTime != 0) {
                    this.voteTime = 0
                }
                this.countedUsers = []
                this.votingIsRunning = false
                this.voteResult = Math.round(
                    (this.messageSum / this.messageCount) * 100
                ) / 100
                if(isNaN(this.voteResult)) {
                    this.voteResult = 0
                }
                this.bot.action(channel,
                    'voting ended, votes: ' + this.messageCount
                    + ', result: '  + this.voteResult)
            },
            onMessageHandler(target, context, message, self, currentLocalVoteInstance) {
                if(self) {
                    return
                }
                let votedUserSearch = currentLocalVoteInstance.countedUsers.find(user => user === context.username)
                let userAlreadyVote = votedUserSearch != null
                if (!isNaN(message) && !userAlreadyVote
                    && Number(message) >= currentLocalVoteInstance.downBorder
                    && Number(message) <= currentLocalVoteInstance.upBorder
                    && currentLocalVoteInstance.votingIsRunning
                    && target.replace('#','') === currentLocalVoteInstance.channel) {

                    let newVotedUsers = _.clone(currentLocalVoteInstance.countedUsers)
                    newVotedUsers.push(context.username)
                    currentLocalVoteInstance.countedUsers = newVotedUsers
                    currentLocalVoteInstance.messageCount++
                    currentLocalVoteInstance.messageSum += Number(message)
                    currentLocalVoteInstance.voteResult = Math.round(
                        (currentLocalVoteInstance.messageSum / currentLocalVoteInstance.messageCount) * 100
                    ) / 100
                }
            }
        }
    }
</script>

<style scoped>

</style>