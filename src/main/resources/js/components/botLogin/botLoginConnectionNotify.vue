<template>
    <div>
        <div v-if="joinedChannels.length > 0" v-for="joinedChannel in joinedChannels">
            <span style="color: green">
                successful join channel: {{joinedChannel}}
            </span>
        </div>
        <div v-if="failedJoinChannels.length > 0" v-for="failedChannel in failedJoinChannels">
            <span style="color: red">
                failed join channel: {{failedChannel.channel}}
                    <span class="mt-2">
                        ({{failedChannel.error}})
                    </span>
            </span>
        </div>
    </div>
</template>

<script>
    import {mapState} from 'vuex'

    export default {
        name: "botLoginChannelConnectionNotify",
        computed: {
            ...mapState(['joinedChannels', 'failedJoinChannels']),
        }
    }
</script>

<style scoped>

</style>