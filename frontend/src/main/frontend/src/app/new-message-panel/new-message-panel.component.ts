import { Component } from '@angular/core';
import { MessagesService } from '../messages.service';


@Component({
  selector: 'new-message-panel',
  templateUrl: './new-message-panel.component.html'
})
export class NewMessagePanelComponent {
  queues: Array<string> = [];

  constructor(private messagesService: MessagesService) {
    messagesService.getOutgoingQueues().subscribe(
      result => {
        this.queues = result;
      }
    );
  }
}
