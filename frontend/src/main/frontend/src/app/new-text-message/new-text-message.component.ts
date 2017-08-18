import { Component } from '@angular/core';
import { MessagesService } from '../messages.service';


@Component({
  selector: 'new-text-message',
  templateUrl: './new-text-message.component.html'
})
export class NewTextMessageComponent {
  properties: Array<string> = [];
  selectedQueue: string;
  messageBody: string;
  count: number = 1;

  constructor(private messagesService: MessagesService) {

    messagesService.getNewMessageProperties().subscribe(
      result => {
        this.properties = result;
      }
    );
  }

  onSubmit(form: any): void {
    const props: any = new Object();

    for (const property of this.properties) {
      if (form['property.' + property]) {
        props[property] = form['property.' + property];
      }
    }

    this.messagesService.sendMessage(this.selectedQueue, this.messageBody, this.count, props).subscribe();
    this.messageBody = '';
  }

}
