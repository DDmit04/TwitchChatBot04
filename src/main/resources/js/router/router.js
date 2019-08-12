import Vue from 'vue'
import VueRouter from 'vue-router'
import botLogin from '../components/botLogin/botLogin.vue'
import voting from '../pages/voting.vue'
import commands from '../pages/commands.vue'
import channelsNavbar from '../components/channelsNavbar.vue'

Vue.use(VueRouter)

const routes = [
    { path: '/', component: botLogin },
    { path: '/commands', component: channelsNavbar,
        children: [
            { path: ':channel', component: commands, props: true },
        ]
    },
    { path: '/voting', component: channelsNavbar,
        children: [
            { path: ':channel', component: voting, props: true },
        ]
    },
    { path: '*',  component: botLogin },
]

export default new VueRouter({
    mode: 'history',
    routes: routes
})