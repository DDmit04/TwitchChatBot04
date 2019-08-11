<template>
    <div>
        <h3 class="my-3">chat voting for channel: #{{channel}}</h3>
        <div class="row">
            <keep-alive>
                <messageVoting class="col" :channel="channel"/>
            </keep-alive>
            <keep-alive>
                <messageCounting class="col" :channel="channel"/>
            </keep-alive>
        </div>
    </div>
</template>

<script>
    import messageVoting from '../components/votes/messageVoting.vue'
    import messageCounting from '../components/votes/messageCounting.vue'

    export default {
        name: "voting",
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
            messageVoting,
            messageCounting
        }
    }
</script>

<style scoped>

</style>