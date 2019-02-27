import { SimpleMessage } from "./simple-message";

export class MessageResult {
    constructor(public messages: Array<SimpleMessage>, public lastId: number) { 

    }
}
