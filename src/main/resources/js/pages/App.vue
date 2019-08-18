<template>
    <div>
        <nav class="navbar navbar-expand-lg navbar-primary navbar-default bg-light shadow">
            <div class="container-fluid">
                <ul class="navbar-nav mr-auto">
                    <li class="nav-item">
                        <router-link class="nav-link" to="/">bot login</router-link>
                    </li>
                    <li class="nav-item">
                        <router-link class="nav-link" to="/commands">commands</router-link>
                    </li>
                    <li class="nav-item">
                        <router-link class="nav-link" to="/voting">voting</router-link>
                    </li>
                    <li class="nav-item">
                        <feedbackModal/>
                    </li>
                </ul>
                <span class="badge mx-2" :class="connectionBadgeClass">{{connectionBadgeText}}</span>
                <div class="mr-3">{{currentUsername}}</div>
                <a v-if="currentUser == null" class="btn btn-primary" href="/login">login</a>
                <a v-else class="btn btn-primary" href="/logout">logout</a>
            </div>
        </nav>
        <div class="container-fluid">
            <router-view></router-view>
        </div>
    </div>
</template>
<script>
    import {mapState} from 'vuex'
    import feedbackModal from '../components/feedbackModal.vue'

    export default {
        components: {
            feedbackModal
        },
        computed: {
            ...mapState(['failedJoinChannels', 'joinedChannels', 'currentUser']),
            currentUsername() {
                return this.currentUser == null ? 'unknown' : this.currentUser.name
            },
            connectionBadgeClass() {
                let resClass
                if (this.joinedChannels.length == 0 && this.failedJoinChannels.length != 0) {
                    resClass = 'badge-danger'
                } else if (this.joinedChannels.length != 0 && this.failedJoinChannels.length != 0) {
                    resClass = 'badge-warning'
                } else if(this.joinedChannels.length == 0 && this.failedJoinChannels.length == 0){
                    resClass = 'badge-secondary'
                } else {
                    resClass = 'badge-success'
                }
                return resClass
            },
            connectionBadgeText() {
                let resText
                if (this.joinedChannels.length === 0 && this.failedJoinChannels.length != 0) {
                    resText = 'bot connection error'
                } else if (this.joinedChannels.length != 0 && this.failedJoinChannels.length != 0) {
                    resText = 'bot connected with warnings'
                } else if(this.joinedChannels.length == 0 && this.failedJoinChannels.length == 0){
                    resText = 'bot not active'
                } else {
                    resText = 'bot connected'
                }
                return resText
            }
        }
    }
</script>
<style>

</style>