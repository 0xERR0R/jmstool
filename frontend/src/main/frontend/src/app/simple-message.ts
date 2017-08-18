export class SimpleMessage {
  constructor(public id: number, public timestamp: Date,
    private queue: String, public text: String,
    public props: any, public isNew: boolean) {
  }
}
