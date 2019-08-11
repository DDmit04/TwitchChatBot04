import Vue from 'vue'

const feedback = Vue.resource('/feedback')

export default {
    add: message => feedback.save({}, message)
}