<template>
    <div>
        <b-card class="shadow">
            <div slot="header">
                <b-button class="btn-block mt-2"
                          v-b-toggle.regularCommandCollapse
                          :disabled="commands.length == 0"
                          variant="primary">
                    regular commands status
                </b-button>
                <b-collapse class="mt-2" id="regularCommandCollapse">
                    <ul class="list-group">
                        <li v-for="(command, index) in commands"
                            class="list-group-item d-flex justify-content-between align-items-center mt-1"
                            id="commandListElement"
                            @click.self="selectedCommand = index">
                            <span class="ml-1">
                                {{command.commandCall}}
                                <button class="btn btn-primary ml-2"
                                        @click="commandRunControl(command)">
                                    <span v-if="!command.commandIsRunning">
                                        run
                                    </span>
                                    <span v-else>
                                        stop
                                    </span>
                                </button>
                                <button class="btn btn-primary"
                                        :disabled="!command.commandIsRunning"
                                        @click="restartCommand(command)">
                                    rerun
                                </button>
                            </span>
                            <span v-if="!command.commandIsRunning" class="badge badge-danger">stopped</span>
                            <span v-else class="badge badge-success">running</span>
                            <span class="badge badge-primary badge-pill">{{convertSecToTime(command)}}</span>
                        </li>
                    </ul>
                </b-collapse>
                current command timer:
                <span class="badge badge-primary badge-pill ml-2">{{convertSecToTime(currentCommand)}}</span>
            </div>
            <b-card-text>
                <b-form-select class="mt-2" v-model="selectedCommand">
                    <option :value="null" disabled>-- select command --</option>
                    <option :value="index" v-for="(command, index) in commands">
                        {{command.commandCall}}
                        <span v-if="!command.commandIsRunning">
                            ( stopped )
                        </span>
                    </option>
                    <option :value="'newCommand'">-- create new command --</option>
                </b-form-select>
                <div class="form-group">
                    <input class="form-control mt-2"
                           v-model="commandCallInput"
                           maxlength="30"
                           :disabled="optionNotSelected || commandIsRunning"
                           type="text"
                           name="command call"
                           v-validate="{required: commandCallInput != null}"
                           placeholder="command"/>
                    <span style="color: red">{{ errors.first('command call') }}</span>
                    <span v-if="commandCallAlreadyExists && !commandEdited && selectedCreateCommandOption"
                          style="color: red">
                    command with call {{commandCallInput}} already exist!
                </span>
                    <input class="form-control mt-2"
                           v-model="commandActionInput"
                           :disabled="optionNotSelected || commandIsRunning"
                           type="text"
                           name="command action"
                           v-validate="{required: commandActionInput != null}"
                           placeholder="command action"/>
                    <span style="color: red">{{ errors.first('command action') }}</span>
                    <input class="form-control mt-2"
                           v-model="commandCoolDownInput"
                           :disabled="optionNotSelected || commandIsRunning"
                           type="number"
                           name="command coll down"
                           v-validate="{ required: commandCoolDownInput != null, min_value: 1 }"
                           placeholder="cool down (sec)"/>
                    <span style="color: red">{{ errors.first('command coll down') }}</span>
                </div>
                <button v-if="selectedCreateCommandOption || optionNotSelected"
                        class="btn btn-primary"
                        :disabled="!canAddCommand"
                        @click="addCommand">
                    add command
                </button>
                <button v-else
                        class="btn btn-primary"
                        :disabled="!canEditCommand"
                        @click="editCommand">
                    edit Command
                </button>
                <button class="btn btn-primary"
                        :disabled="optionNotSelected || selectedCreateCommandOption || commandIsRunning"
                        @click="removeCommand">
                    remove Command
                </button>
                <div class="mt-2">
                    <button class="btn btn-primary"
                            :disabled="commands.length == 0"
                            @click="startAllCommands">
                        start all commands
                    </button>
                    <button class="btn btn-primary"
                            :disabled="!someCommandIsRunning || commands.length == 0"
                            @click="stopAllCommands">
                        stop all commands
                    </button>
                    <button class="btn btn-primary"
                            :disabled="commands.length == 0"
                            @click="restartAllCommands">
                        restart all commands
                    </button>
                </div>
            </b-card-text>
        </b-card>
    </div>
</template>

<script>
    import {mapState, mapActions, mapMutations} from 'vuex'
    import {convertSecToTime} from 'helpers/convertSecToTime'
    import _ from 'lodash'

    export default {
        name: "regularCommands",
        props: ['channel'],
        data() {
            return {
                localInstances: [],
                defaultGlobalInstance: {
                    commands: [],
                    commandCallInput: null,
                    commandActionInput: null,
                    commandCoolDownInput: 60,
                    selectedCommand: null,
                },
                defaultLocalInstance: {
                    channel: null,
                    commandBuffer: null,
                },
            }
        },
        created() {
            if(this.regularCommandInstances.length == 0) {
                for (let i = 0; i < this.joinedChannels.length; i++) {
                    let savedChannelsSearch = this.savedChannels.find(channel => channel.channelName == this.joinedChannels[i])
                    let globalRegularCommandInstance
                    if (savedChannelsSearch.regularCommandInstance == null) {
                        globalRegularCommandInstance = _.clone(this.defaultGlobalInstance)
                        this.pushNewRegularCommandInstanceAction({
                            instance: globalRegularCommandInstance,
                            channel: this.joinedChannels[i]
                        })
                    } else {
                        globalRegularCommandInstance = _.cloneDeep(savedChannelsSearch.regularCommandInstance)
                    }
                    let newLocalInstance = _.clone(this.defaultGlobalInstance)
                    newLocalInstance = Object.assign(newLocalInstance, globalRegularCommandInstance)
                    newLocalInstance.channel = this.joinedChannels[i]
                    this.localInstances.push(newLocalInstance)
                    this.pushNewLocalRegularCommandInstanceMutation(newLocalInstance.channel)
                }
            }
        },
        computed: {
            ...mapState(['bot', 'joinedChannels', 'savedChannels']),
            ...mapState('saveChannelsData', ['savedChannels']),
            ...mapState('localInstances', ['regularCommandInstances']),
            currentChannel() { return this.currentLocalCommandInstance.channel },
            selectedCreateCommandOption() { return this.selectedCommand === 'newCommand' },
            optionNotSelected() { return this.selectedCommand == null },
            currentCommand() { return this.commands[this.selectedCommand] },
            someCommandIsRunning() {
                let running = false
                this.commands.forEach(command => {
                    if(command.commandIsRunning) {
                        running = true
                    }
                })
                return running
            },
            canAddCommand() {
                return (
                    !this.commandCallAlreadyExists
                    && !this.optionNotSelected
                    && this.commandCallInput != null
                    && this.commandActionInput != null
                    && this.commandCoolDownInput != null
                )
            },
            canEditCommand() {
                return (
                    !this.commandCallAlreadyExists
                    || this.commandEdited
                )
            },
            commandCallAlreadyExists() {
                let commandCallSearch = this.commands.find(
                    command => command.commandCall === this.commandCallInput
                )
                return (commandCallSearch != null)
            },
            commandEdited() {
                let commandEdited = false
                if(this.commandBuffer != null) {
                    commandEdited = (this.commandBuffer.commandCall != this.commandCallInput
                        || this.commandBuffer.commandAction != this.commandActionInput
                        || this.commandBuffer.commandCoolDown != this.commandCoolDownInput)
                }
                return commandEdited
            },
            currentLocalCommandInstance() {
                let commandInstanceSearch = this.localInstances.find(inst => inst.channel == this.channel)
                let instance = null
                if (commandInstanceSearch == null) {
                    instance = _.clone(this.defaultGlobalInstance)
                    instance = Object.assign(instance, this.defaultLocalInstance)
                } else {
                    instance = commandInstanceSearch
                }
                return instance
            },
            updatedGlobalInstance() {
                return {
                    id: this.currentLocalCommandInstance.id,
                    commands: _.cloneDeep(this.commands),
                    commandCallInput: this.commandCallInput,
                    commandActionInput: this.commandActionInput,
                    commandCoolDownInput: this.commandCoolDownInput,
                    selectedCommand: this.selectedCommand,
                }
            },
            selectedCommand: {
                get() { return this.currentLocalCommandInstance.selectedCommand },
                set(newVal) {
                    if(newVal != 'newCommand' && newVal != null) {
                        this.saveCommandToBuffer(newVal)
                        this.commandCallInput = this.commands[newVal].commandCall
                        this.commandActionInput = this.commands[newVal].commandAction
                        this.commandCoolDownInput = this.commands[newVal].commandCoolDown
                    } else {
                        this.commandBuffer = null
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
            commands: {
                get() { return this.currentLocalCommandInstance.commands },
                set(newVal) {
                    this.currentLocalCommandInstance.commands = newVal
                    this.updateGlobalInstance()
                }
            },
            commandIsRunning: {
                get() {
                    let isRunning = false
                    if(this.selectedCommand != null && this.selectedCommand != 'newCommand') {
                        isRunning = this.commands[this.selectedCommand].commandIsRunning
                    }
                    return isRunning
                },
                set(newVal) { this.commands[this.selectedCommand].commandIsRunning = newVal }
            }
        },
        methods: {
            ...mapActions('saveChannelsData', ['pushNewRegularCommandInstanceAction', 'updateRegularCommandInstanceAction']),
            ...mapMutations('localInstances', ['pushNewLocalRegularCommandInstanceMutation']),
            updateGlobalInstance() {
                this.updateRegularCommandInstanceAction({
                    updatedInstance: this.updatedGlobalInstance,
                    channel: this.currentChannel
                })
            },
            editCommand() {
                this.commands[this.selectedCommand].commandCall = this.commandCallInput
                this.commands[this.selectedCommand].commandAction = this.commandActionInput
                this.commands[this.selectedCommand].commandCoolDown = this.commandCoolDownInput
                this.commands[this.selectedCommand].commandTime = this.commandCoolDownInput
                this.saveCommandToBuffer(this.selectedCommand)
            },
            removeCommand() {
                this.commands.splice(this.selectedCommand, 1)
                this.selectedCommand = this.defaultGlobalInstance.selectedCommand
            },
            addCommand() {
                let newCommand = {
                    commandCall: this.commandCallInput,
                    commandAction: this.commandActionInput,
                    commandCoolDown: this.commandCoolDownInput,
                    commandIsRunning: false,
                    commandTime: this.commandCoolDownInput,
                    commandTimer: null,
                    method: null,
                }
                let newCommands = this.commands
                newCommands.push(newCommand)
                this.commands = newCommands
                this.selectedCommand = this.commands.indexOf(newCommand)
            },
            saveCommandToBuffer(selectedCommand) {
                this.commandBuffer = _.clone(this.commands[selectedCommand])
                this.commandBuffer.commandAccess = _.clone(this.commands[selectedCommand].commandAccess)
            },
            commandRunControl(command) {
                if (command.commandIsRunning) {
                    this.stopCommand(command)
                } else {
                    this.startCommand(this.currentChannel, command)
                }
            },
            restartAllCommands() {
                this.commands.forEach(command => this.restartCommand(command))
            },
            startAllCommands() {
                this.commands.forEach(command => {
                    if(!command.commandIsRunning) {
                        this.startCommand(this.currentChannel, command)
                    }
                })
            },
            stopAllCommands() {
                this.commands.forEach(command => {
                    if(command.commandIsRunning) {
                        this.stopCommand(command)
                    }
                })
            },
            startCommand(channel, command) {
                command.commandIsRunning = true
                command.commandTimer = setInterval(() => {
                    command.commandTime--
                    if(command.commandTime == 0) {
                        this.bot.action(channel, command.commandAction)
                        command.commandTime = command.commandCoolDown
                    }
                }, 1000)
            },
            stopCommand(command) {
                clearInterval(command.commandTimer)
                command.commandIsRunning = false
            },
            restartCommand(command) {
                this.stopCommand(command)
                command.commandTime = command.commandCoolDown
                this.startCommand(this.currentChannel, command)
            },
            convertSecToTime(command) {
                let convertedTime = '00:00:00'
                if(command != null) {
                    convertedTime = convertSecToTime(command.commandTime)
                }
                return convertedTime
            },
        }
    }
</script>

<style scoped>
#commandListElement:hover {
    outline: 2px solid rgb(0,150,255)
}
</style>