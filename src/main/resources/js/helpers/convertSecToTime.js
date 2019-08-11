export function convertSecToTime(secTime) {
    let hour = '00'
    let min = '00'
    let sec = '00'
    hour = Math.trunc(secTime / 3600)
    min = Math.trunc((secTime - hour * 3600) / 60)
    sec = secTime - min * 60 - hour * 3600
    if(sec / 10 < 1) {
        sec = '0' + String(sec)
    }
    if(min / 10 < 1) {
        min = '0' + String(min)
    }
    if(hour / 10 < 1) {
        hour = '0' + String(hour)
    }
    return hour + ':' + min + ':' + sec
}