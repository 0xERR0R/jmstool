import { Component, ViewChild } from '@angular/core';
import { ModalDirective } from 'ngx-bootstrap';
import { SimpleMessage } from '../simple-message';

@Component({
  selector: 'message-modal-dialog',
  templateUrl: './message-modal-dialog.component.html'
})

export class MessageModalDialogComponent {
  messageToShow: SimpleMessage;
  formatMessage = true;

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
