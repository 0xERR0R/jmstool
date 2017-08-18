import { Component, Output, EventEmitter } from '@angular/core';
import { MessagesService } from '../messages.service';


@Component({
  selector: 'outgoinig-queues-select',
  templateUrl: './outgoing-queues-select.component.html'
})
export class OutgoingQueuesSelectComponent {
  @Output("value")
  public valueEvent: EventEmitter<string> = new EventEmitter();

  queues: Array<string> = [];
  selectedQueue: string;

  constructor(private messagesService: MessagesService) {
    messagesService.getOutgoingQueues().subscribe(
      result => {
        this.queues = result;
        this.selectedQueue = this.queues[0];
        this.emitValue(this.selectedQueue);
      }
    );
  }

  public emitValue(value: string): void {
        this.valueEvent.emit(value);
    }
}
