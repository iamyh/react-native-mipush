import {NativeModules, DeviceEventEmitter} from 'react-native';

const nativeModule = NativeModules.MiPush;
const invariant = require('invariant');

const _notifHandlers = [];

export default class MiPushNotification {

    _data;

    constructor(nativeNotif) {
        this._data = {};

        if (typeof nativeNotif === 'string') {
            nativeNotif = JSON.parse(nativeNotif)
        }

        if (nativeNotif) {
            this._data = nativeNotif
        }
    }

    static setAlias(alias){
        if (!alias) {
            alias = ''
        }
        nativeModule.setAlias(alias)
    }

    static unsetAlias(alias){
        if (!alias) {
            alias = ''
        }
        nativeModule.unsetAlias(alias)
    }

    static setTags(tags, alias){
        if (!alias) {
            alias = ''
        }
        nativeModule.setTags(tags, alias)
    }

    static getRegistrationID(){
        return new Promise(resolve=>{
            nativeModule.getRegistrationID(resolve)
        })
    }

    static addEventListener(type: string, handler: Function) {
        checkListenerType(type)

        const listener = DeviceEventEmitter.addListener(
            type,
            (note) => {
                handler(note && new MiPushNotification(note));
            }
        );
        _notifHandlers.push(listener)
        return listener;
    }

    static removeEventListener(listener) {
        const index = _notifHandlers.indexOf(listener)
        if (index >=0) {
            _notifHandlers.splice(index,1)
            listener.remove()
        }
    }

    // ios only
    static registerMiPushAndConnect(){
        nativeModule.registerMiPushAndConnect && nativeModule.registerMiPushAndConnect(true, 15)
    }

    // ios only
    static isOpen(){
        if (nativeModule.isOpen){
            return nativeModule.isOpen();
        }
    }
    
}

function checkListenerType(type) {
    invariant(
        type === "mipush",
        'not support type=' + type
    );
}


