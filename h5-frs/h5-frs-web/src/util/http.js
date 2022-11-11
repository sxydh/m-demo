import axios from 'axios'

const service = axios.create({
    timeout: 1000000
})

export default service