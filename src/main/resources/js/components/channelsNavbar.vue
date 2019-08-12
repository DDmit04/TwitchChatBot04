<template>
    <div class="mt-2">
        <ul class="nav nav-tabs">
            <li v-for="channel in joinedChannels" class="nav-item">
                <router-link class="nav-link" :to="currentPath + '/' + channel">
                    <div @click="updateLastSelectedChanel(channel)">#{{channel}}</div>
                </router-link>
            </li>
        </ul>
        <router-view></router-view>
    </div>
</template>

<script>
    import {mapState, mapMutations} from 'vuex'

    export default {
        name: "channelsNavbar",
        data() {
            return {
                currentPath: null,
            }
        },
        computed: {
            ...mapState(['joinedChannels']),
            ...mapState('channelsNavbar', ['lastSelectedChanelCommands', 'lastSelectedChanelVote']),
            joinedChannelsExists() {
                return this.joinedChannels.length > 0
            }
        },
        watch: {
            '$route' (to, from) {
                this.currentPath = to.matched[0].path
                let finalPath = this.currentPath + '/'
                if(to.path.match('voting')) {
                    if(this.lastSelectedChanelVote == null) {
                        if(this.joinedChannelsExists) {
                            finalPath += this.joinedChannels[0]
                        }
                    } else {
                        finalPath += this.lastSelectedChanelVote
                    }
                } else if(to.path.match('command')) {
                    if(this.lastSelectedChanelCommands == null) {
                        if(this.joinedChannelsExists) {
                            finalPath += this.joinedChannels[0]
                        }
                    } else {
                        finalPath += this.lastSelectedChanelCommands
                    }
                }
                this.$router.push(finalPath).catch(err => {})
            }
        },
        created() {
            this.currentPath = this.$route.path
            if(this.joinedChannelsExists) {
                this.$router.push(this.currentPath + '/' + this.joinedChannels[0])
            }
        },
        methods: {
            ...mapMutations('channelsNavbar', ['updateSelectedChanelCommandsMutation', 'updateSelectedChanelVoteMutation']),
            updateLastSelectedChanel(channel) {
                if(this.currentPath.match('command')){
                    this.updateSelectedChanelCommandsMutation(channel)
                } else if(this.currentPath.match('voting')) {
                    this.updateSelectedChanelVoteMutation(channel)
                }
            }
        }
    }
</script>

<style scoped>

</style>