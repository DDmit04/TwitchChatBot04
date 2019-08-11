<template>
    <div>
        <b-card class="shadow">
            <div slot="header">
                <b-button class="btn-block mb-2"
                          v-b-toggle.regularCommandsStatusCollapse
                          :disabled="commands.length == 0"
                          variant="primary">
                    colling commands status
                </b-button>
                <b-collapse class="mb-2" id="regularCommandsStatusCollapse">
                    <ul class="list-group">
                        <li v-for="(command, index) in commands"
                            class="list-group-item d-flex justify-content-between align-items-center  mt-1"
                            id="commandListElement"
                            @click="selectedCommand = index">
                            <span>
                                {{command.commandCall}}
                                <button class="btn btn-primary ml-1"
                                        @click="disableCommandControl(command)">
                                    <span v-if="command.commandDisabled">
                                        unable Command
                                    </span>
                                    <span v-else>
                                        disable command
                                    </span>
                                </button>
                            </span>
                            <div v-if="anybodyCanUseCommand(command)">
                                <span class="badge badge-success">
                                    <div v-for="(access, accessSubject) in command.commandAccess">
                                        <span v-if="access">
                                            {{accessSubject}}
                                        </span>
                                    </div>
                                </span>
                            </div>
                            <div v-else>
                                <span class="badge badge-danger">broadcaster only</span>
                            </div>
                            <span v-if="command.commandDisabled" class="badge badge-danger">commandDisabled</span>
                            <span v-else class="badge badge-success">unable</span>
                        </li>
                    </ul>
                </b-collapse>
            </div>
            <b-card-text>
                <b-form-select class="mb-2" v-model="selectedCommand">
                    <option :value="null" disabled>
                        -- select command --
                    </option>
                    <option :value="index" v-for="(command, index) in commands">
                        {{command.commandCall}}
                        <span v-if="command.commandDisabled">
                            ( commandDisabled )
                        </span>
                    </option>
                    <option :value="'newCommand'">
                        -- create new command --
                    </option>
                </b-form-select>
                <b-button class="btn-block mb-2"
                          v-b-toggle.collingCommandCollapse
                          :disabled="optionNotSelected"
                          variant="primary">
                    command access
                </b-button>
                <b-collapse class="mb-2" id="collingCommandCollapse">
                    <ul class="list-group">
                        <li class="list-group-item">
                            <b-form-checkbox switch v-model="commandFullAccess">all</b-form-checkbox>
                        </li>
                        <li class="list-group-item">
                            <b-form-checkbox switch v-model="commandAccessInput.moderator">moderator</b-form-checkbox>
                        </li>
                        <li class="list-group-item">
                            <b-form-checkbox switch v-model="commandAccessInput.subscriber">subscriber</b-form-checkbox>
                        </li>
                        <li class="list-group-item">
                            <b-form-checkbox switch v-model="commandAccessInput.unSubscriber">unSubscriber</b-form-checkbox>
                        </li>
                    </ul>
                </b-collapse>
                <div class="form-group">
                    <input class="form-control mb-2"
                           maxlength="30"
                           :disabled="optionNotSelected || selectedCommand == 0"
                           v-model="commandCallInput"
                           type="text"
                           name="command call"
                           v-validate="{required: commandCallInput != null}"
                           placeholder="command"/>
                    <span style="color: red">{{ errors.first('command call') }}</span>
                    <span v-if="commandCallAlreadyExists
                            && !commandAccessEdited
                            && !commandEdited
                            && selectedCreateCommandOption"
                          style="color: red">
                    command with call {{commandCallInput}} already exist!
                </span>
                    <input class="form-control mb-2"
                           :disabled="optionNotSelected  || selectedCommand == 0"
                           v-model="commandActionInput"
                           type="text"
                           name="action"
                           v-validate="{required: commandActionInput != null}"
                           placeholder="command action"/>
                    <span style="color: red">{{ errors.first('command action') }}</span>
                    <input class="form-control mb-2"
                           :disabled="optionNotSelected"
                           v-model="commandCoolDownInput"
                           type="number"
                           placeholder="cool down (sec)"/>
                </div>
                <button v-if="optionNotSelected || selectedCreateCommandOption"
                        class="btn btn-primary"
                        :disabled="!canAddCommand"
                        @click="addCommand">add command
                </button>
                <button v-else
                        class="btn btn-primary"
                        :disabled="!canEditCommand"
                        @click="editCommand">edit Command
                </button>
                <button class="btn btn-primary"
                        :disabled="optionNotSelected || selectedCreateCommandOption || selectedCommand == 0"
                        @click="removeCommand">remove Command
                </button>
            </b-card-text>
        </b-card>
    </div>
</template>

<script>
    import {mapState, mapActions} from 'vuex'
    import _ from 'lodash'

    export default {
        name: "collingCommands",
        props: ['channel'],
        data() {
            return {
                localInstances: [],
                defaultLocalInstance: {
                    channel: null,
                    commandBuffer: null,
                },
                defaultGlobalInstance: {
                    commands: [],
                    commandCallInput: null,
                    commandActionInput: null,
                    commandCoolDownInput: 60,
                    selectedCommand: null,
                    commandAccessInput: {
                        moderator: true,
                        subscriber: true,
                        unSubscriber: true
                    }
                },
            }
        },
        created() {
            for (let i = 0; i < this.joinedChannels.length; i++) {
                let savedChannelsSearch = this.savedChannels.find(channel => channel.channelName == this.joinedChannels[i])
                let globalCollingCommandInstance
                if (savedChannelsSearch.callingCommandInstance == null) {
                    globalCollingCommandInstance = _.cloneDeep(this.defaultGlobalInstance)
                    let overViewCommand = {
                        commandCall: '!commands',
                        commandAction: 'no commands yet',
                        commandCoolDown: this.defaultGlobalInstance.commandCoolDownInput,
                        commandAccess: {...this.defaultGlobalInstance.commandAccessInput},
                        commandIsReady: true,
                        commandDisabled: false,
                        method: null
                    }
                    globalCollingCommandInstance.commands.push(overViewCommand)
                    this.pushNewCollingCommandInstanceAction({
                        instance: globalCollingCommandInstance,
                        channel: this.joinedChannels[i]
                    })
                } else {
                    globalCollingCommandInstance = _.cloneDeep(savedChannelsSearch.callingCommandInstance)
                }
                let newLocalInstance = _.cloneDeep(this.defaultLocalInstance)
                newLocalInstance = Object.assign(newLocalInstance, globalCollingCommandInstance)
                newLocalInstance.channel = this.joinedChannels[i]
                this.localInstances.push(newLocalInstance)
                this.bot.on('chat', (target, context, message, self) => {
                    this.chatCommandsHandler(target, context, message, self, newLocalInstance)
                })
            }
        },
        computed: {
            ...mapState(['bot', 'joinedChannels']),
            ...mapState('saveChannelsData', ['savedChannels']),
            selectedCreateCommandOption() { return this.selectedCommand === 'newCommand' },
            optionNotSelected() { return this.selectedCommand == null },
            currentChannel() { return this.currentLocalCommandInstance.channel },
            currentLocalCommandInstance() {
                let commandInstanceSearch = this.localInstances.find(inst => inst.channel == this.channel)
                let instance = null
                if (commandInstanceSearch == null) {
                    instance = _.cloneDeep(this.defaultGlobalInstance)
                    instance = Object.assign(instance, this.defaultLocalInstance)
                } else {
                    instance = commandInstanceSearch
                }
                return instance
            },
            updatedGlobalInstance() {
                return {
                    commands: _.cloneDeep(this.commands),
                    commandCallInput: this.commandCallInput,
                    commandActionInput: this.commandActionInput,
                    commandCoolDownInput: this.commandCoolDownInput,
                    selectedCommand: this.selectedCommand,
                    commandAccessInput: this.commandAccessInput
                }
            },
            canAddCommand() {
                return (
                    !this.commandCallAlreadyExists
                    && !this.optionNotSelected
                    && this.commandCallInput != null
                    && this.commandActionInput != null
                )
            },
            canEditCommand() {
                return (
                    !this.commandCallAlreadyExists
                    || this.commandEdited
                    || this.commandAccessEdited
                )
            },
            commandFullAccess: {
                get() {
                    let fullAccess = true
                    for(let key in this.commandAccessInput) {
                        if(this.commandAccessInput[key] == false) {
                            fullAccess = false
                        }
                    }
                    return fullAccess
                },
                set(newVal) {
                    if(newVal == true) {
                        for(let key in this.commandAccessInput) {
                            this.currentLocalCommandInstance.commandAccessInput[key] = true
                        }
                    }
                }
            },
            commandCallAlreadyExists() {
                let commandCallSearch = this.currentLocalCommandInstance.commands.find(
                    command => command.commandCall === this.commandCallInput
                )
                return (commandCallSearch != null)
            },
            commandEdited() {
                let commandEdited = false
                if(this.commandBuffer != null) {
                    commandEdited = (
                        this.commandBuffer.commandCall != this.commandCallInput
                        || this.commandBuffer.commandAction != this.commandActionInput
                        || this.commandBuffer.commandCoolDown != this.commandCoolDownInput
                    )
                }
                return commandEdited
            },
            commandAccessEdited() {
                let commandAccessEdited = false
                if(this.commandBuffer != null) {
                    commandAccessEdited =
                        JSON.stringify(this.commandBuffer.commandAccess) !== JSON.stringify(this.commandAccessInput)
                }
                return commandAccessEdited
            },
            selectedCommand: {
                get() { return this.currentLocalCommandInstance.selectedCommand },
                set(newVal) {
                    if (newVal != 'newCommand' && newVal != null) {
                        this.saveCommandToBuffer(newVal)
                        this.commandAccessInput = {...this.commands[newVal].commandAccess}
                        this.commandCallInput = this.commands[newVal].commandCall
                        this.commandActionInput = this.commands[newVal].commandAction
                        this.commandCoolDownInput = this.commands[newVal].commandCoolDown
                    } else {
                        this.commandBuffer = null
                        this.commandAccessInput = {...this.defaultGlobalInstance.commandAccessInput}
                        this.commandCallInput = this.defaultGlobalInstance.commandCallInput
                        this.commandActionInput = this.defaultGlobalInstance.commandActionInput
                        this.commandCoolDownInput = this.defaultGlobalInstance.commandCoolDownInput
                    }
                    this.currentLocalCommandInstance.selectedCommand = newVal
                    this.updateGlobalInstance()
                }
            },
            commandCallInput: {
                get() { return this.currentLocalCommandInstance.commandCallInput },
                set(newVal) {
                    this.currentLocalCommandInstance.commandCallInput = newVal
                    this.updateGlobalInstance()
                }
            },
            commandActionInput: {
                get() { return this.currentLocalCommandInstance.commandActionInput },
                set(newVal) {
                    this.currentLocalCommandInstance.commandActionInput = newVal
                    this.updateGlobalInstance()
                }
            },
            commandCoolDownInput: {
                get() { return this.currentLocalCommandInstance.commandCoolDownInput },
                set(newVal) {
                    this.currentLocalCommandInstance.commandCoolDownInput = newVal
                    this.updateGlobalInstance()
                }
            },
            commandAccessInput: {
                get() { return this.currentLocalCommandInstance.commandAccessInput },
                set(newVal) {
                    this.currentLocalCommandInstance.commandAccessInput = newVal
                    this.updateGlobalInstance()
                }
            },
            commands: {
                get() {
                    return this.currentLocalCommandInstance.commands },
                set(newVal) {
                    this.currentLocalCommandInstance.commands = newVal
                    this.updateGlobalInstance()
                }
            },
            commandsOverViewCommand: {
                get() { return this.commands.find(command => command.commandCall == '!commands') },
                set(newVal) {
                    let overViewCommandSearch = this.commands.find(command => command.commandCall == '!commands')
                    overViewCommandSearch = newVal
                    this.updateGlobalInstance()
                }
            },
        },
        methods: {
            ...mapActions('saveChannelsData', ['pushNewCollingCommandInstanceAction', 'updateCollingCommandInstanceAction']),
            updateGlobalInstance() {
                this.updateCollingCommandInstanceAction({
                    updatedInstance: this.updatedGlobalInstance,
                    channel: this.currentChannel
                })
            },
            editCommand() {
                this.commands[this.selectedCommand].commandCall = this.commandCallInput
                this.commands[this.selectedCommand].commandAction = this.commandActionInput
                this.commands[this.selectedCommand].commandCoolDown = this.commandCoolDownInput
                this.commands[this.selectedCommand].commandAccess = {...this.commandAccessInput}
                this.updateCommandOverviewCommand()
                this.saveCommandToBuffer(this.selectedCommand)
            },
            removeCommand() {
                this.commands.splice(this.selectedCommand, 1)
                this.updateCommandOverviewCommand()
                this.selectedCommand = this.defaultGlobalInstance.selectedCommand
            },
            addCommand() {
                let newCommand = {
                    commandCall: this.commandCallInput,
                    commandAction: this.commandActionInput,
                    commandCoolDown: this.commandCoolDownInput,
                    commandAccess: {...this.commandAccessInput},
                    commandIsReady: true,
                    commandDisabled: false,
                    method: null
                }
                let newCommands = this.commands
                newCommands.push(newCommand)
                this.commands = newCommands
                this.updateCommandOverviewCommand()
                this.selectedCommand = this.commands.indexOf(newCommand)
            },
            anybodyCanUseCommand(command) {
                let res = false
                for(let accessSubject in command.commandAccess) {
                    if(command.commandAccess[accessSubject] == true) {
                        res = true
                    }
                }
                return res
            },
            updateCommandOverviewCommand() {
                let allCommandsCalls = []
                this.commands.forEach(command => {
                    if(command.commandCall != '!commands') {
                        allCommandsCalls.push(command.commandCall)
                    }
                })
                let updatedOverView = this.commandsOverViewCommand
                updatedOverView.commandAction = 'commands: ' + allCommandsCalls.toString()
                this.commandsOverViewCommand = updatedOverView
            },
            disableCommandControl(command) {
                command.commandDisabled = !command.commandDisabled
            },
            saveCommandToBuffer(selectedCommand) {
                this.commandBuffer = _.clone(this.commands[selectedCommand])
                this.commandBuffer.commandAccess = _.clone(this.commands[selectedCommand].commandAccess)
            },
            checkCommandAccess(user, commandAccessObject) {
                let accessConfirmed = false
                if (user.badges == null && commandAccessObject.unSubscriber == true) {
                    accessConfirmed = true
                } else if(user.badges != null) {
                    let userBadgesObjectKeys = Object.keys(user.badges)
                    let accessObjectKeys = Object.keys(commandAccessObject)
                    let crossingObjectKeys = userBadgesObjectKeys.filter(
                        badgeObjectKey => accessObjectKeys.includes(badgeObjectKey)
                    )
                    if(crossingObjectKeys.length > 0) {
                        for (let i = 0; i < crossingObjectKeys.length; i++) {
                            let accessObjectKey = crossingObjectKeys[i]
                            let accessObjectValue = accessObjectKeys[accessObjectKey]
                            if(accessObjectValue == true) {
                                accessConfirmed = true
                            }
                        }
                    }
                }
                return accessConfirmed
            },
            userIsBroadcaster(user) {
                let isBroadcaster = false
                if(user.badges != null) {
                    let userBadgesObjectKeys = Object.keys(user.badges)
                    let badgeSearch = userBadgesObjectKeys.find(badge => badge === 'broadcaster')
                    if (badgeSearch != null) {
                        isBroadcaster = true
                    }
                }
                return isBroadcaster
            },
            chatCommandsHandler(target, user, message, self, currentLocalCommandInstance) {
                if(self) {
                    return
                }
                let commandSearch = currentLocalCommandInstance.commands.find(command => command.commandCall === message)
                let commandIsValid = (commandSearch != null && currentLocalCommandInstance.channel === target.replace('#', ''))
                let commandDisabled = false
                if (commandSearch != null) {
                    commandDisabled = commandSearch.commandDisabled
                }
                if (commandIsValid && !commandDisabled) {
                    let userIsBroadcaster = this.userIsBroadcaster(user)
                    let userCanUseCommand = this.checkCommandAccess(user, commandSearch.commandAccess)
                    if (userIsBroadcaster) {
                        this.bot.action(target, commandSearch.commandAction)
                        if (commandSearch.method != null) {
                            commandSearch.method()
                        }
                    } else if (commandSearch.commandIsReady && userCanUseCommand) {
                        this.bot.action(target, commandSearch.commandAction)
                        if (commandSearch.method != null) {
                            commandSearch.method()
                        }
                        commandSearch.commandIsReady = false
                        setTimeout(() => {
                            commandSearch.commandIsReady = true
                        }, commandSearch.commandCoolDown * 1000)
                    }
                }
            },
        }
    }
</script>

<style scoped>
    #commandListElement:hover {
        outline: 2px solid rgb(0,150,255)
    }
</style>