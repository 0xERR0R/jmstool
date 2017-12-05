import { Component, Input, ViewChild, OnDestroy } from '@angular/core';
import { ModalDirective } from 'ngx-bootstrap';
import { NotificationsService } from 'angular2-notifications';
import { Observable } from 'rxjs/Rx';
import { Subscription } from 'rxjs/Subscription';
import { SimpleMessage } from '../simple-message';
import { MessageType } from '../message-type';
import { MessagesService } from '../messages.service';

@Component({
  selector: 'message-modal-dialog',
  templateUrl: './message-modal-dialog.component.html'
})

export class MessageModalDialogComponent 
{
  messageToShow: SimpleMessage;
  formatMessage: boolean = true;

  @ViewChild('childModal') public childModal: ModalDirective;

  public getKeys(obj): string[] {
    return Object.keys(obj);
  }

  showMessage(message: SimpleMessage) {
    console.log('showing message:' + message.text);
    this.messageToShow = message;
    this.childModal.show();
  }

  public hideChildModal(): void {
    this.childModal.hide();
  }
}
