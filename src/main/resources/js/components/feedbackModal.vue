<template>
    <div>
        <a class="nav-link active" v-b-modal.modal-1 href='javascript:void(0)'>feedback</a>
        <b-modal id="modal-1"
                 size="lg"
                 title="send feedback"
                 @cancel='restartModal'>
            <div v-if='feedbackSent'>
                Thanks for yor feedback!
            </div>
            <div v-else-if='feedbackError'>
                Something went wrong with sending feedback please try again later!
            </div>
            <div v-else>
                <form>
                    <div class="form-group">
                        <label for="exampleFormControlInput1">your email address</label>
                        <input class="form-control"
                               v-model='feedbackEmail'
                               id="exampleFormControlInput1"
                               v-validate="'required|email'"
                               name="email"
                               type="email"
                               placeholder="name@example.com">
                        <div>
                            <span class='mt-1' style='color: red'>{{ errors.first('email') }}</span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="exampleFormControlTextarea1">your feedback</label>
                        <textarea class="form-control"
                                  id="exampleFormControlTextarea1"
                                  v-model='feedbackText'
                                  v-validate="'required'"
                                  name='feedText'
                                  type='text'
                                  rows="3">
                        </textarea>
                    </div>
                    <span class='mt-1' style='color: red'>{{ errors.first('feedText') }}</span>
                </form>
            </div>
            <template slot="modal-footer" slot-scope="{ ok, cancel, hide }">
                <div v-if='!feedbackSent'>
                    <b-button size="sm" variant="success" @click="sendFeedback" :disabled='loading || !validate'>
                        <b-spinner v-if='loading'></b-spinner>
                        <div v-else>send</div>
                    </b-button>
                </div>
                <b-button size="sm" variant="danger" @click="cancel()">
                    Cancel
                </b-button>
            </template>
        </b-modal>
    </div>
</template>

<script>
    import api from '../api/feedback'
    import {mapState} from 'vuex'

    export default {
        name: "feedbackModal",
        data() {
            return {
                loading: false,
                feedbackError: false,
                feedbackSent: false,
                feedbackText: '',
                feedbackEmail: '',
            }
        },
        created() {
            if (this.currentUser != null) {
                this.feedbackEmail = this.currentUser.email
            }
        },
        computed: {
            ...mapState(['currentUser']),
            validate() {
                return this.validateEmail(this.feedbackEmail) && this.feedbackText != ''
            }
        },
        methods: {
            async sendFeedback() {
                let feedbackMessage = {
                    id: null,
                    email: this.feedbackEmail,
                    feedbackText: this.feedbackText
                }
                this.loading = true
                const result = await api.add(feedbackMessage)
                if(result.ok) {
                    this.feedbackSent = true
                } else {
                    this.feedbackSent = false
                    this.feedbackError = true
                }
                this.loading = false
                this.feedbackEmail = ''
                this.feedbackText = ''
            },
            restartModal() {
                this.loading = false
                this.feedbackSent = false
                this.feedbackError = false
            },
            validateEmail(email) {
                let regExp = /^([a-z0-9_\.-])+@[a-z0-9-]+\.([a-z]{2,6}\.)?[a-z]{2,6}$/i
                return regExp.test(email)
            }
        }
    }
</script>

<style scoped>

</style>