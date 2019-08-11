<template>
    <div>
        <h3 class="my-3">chat commands for channel: {{channel}}</h3>
        <div class="row">
            <keep-alive>
                <collingCommands class="col" :channel="channel"/>
            </keep-alive>
            <keep-alive>
                <regularCommands class="col" :channel="channel"/>
            </keep-alive>
        </div>
    </div>
</template>

<script>
    import collingCommands from '../components/commands/collingCommands.vue'
    import regularCommands from '../components/commands/regularCommands.vue'

    export default {
        name: "commands",
        props: ['channel'],
        beforeRouteEnter(to, from, next) {
            next(vm => {
                let joinedChannelSearch = vm.$store.state.joinedChannels.find(channel => channel === vm.channel)
                if(joinedChannelSearch == null) {
                    return next(from.path)
                } else {
                    return next()
                }
            })
        },
        components: {
            collingCommands,
            regularCommands
        }
    }
</script>

<style scoped>

</style>
