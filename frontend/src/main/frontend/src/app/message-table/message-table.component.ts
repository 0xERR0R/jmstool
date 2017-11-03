import { Component, OnInit, Input, ViewChild, OnDestroy } from '@angular/core';
import { ModalDirective } from 'ngx-bootstrap';
import { NotificationsService } from 'angular2-notifications';
import { Observable } from 'rxjs/Rx';
import { Subscription } from 'rxjs/Subscription';
import { SimpleMessage } from '../simple-message';
import { MessageType } from '../message-type';
import { MessagesService } from '../messages.service';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'message-table',
  templateUrl: './message-table.component.html'
})

export class MessageTableComponent implements OnInit, OnDestroy {
  public static readonly REFRESH_INTERVAL_MS = 5000;
  public static readonly NEW_MESSAGE_INDICATOR_TIME_MS = 5000;
  public static readonly MAX_MESSAGES_TO_SHOW = 100;

  @Input() type: MessageType;
  @Input() showNotifications: boolean;

  messages: Array<SimpleMessage> = [];
  lastId: number = 0;

  timerSubscription: Subscription;
  constructor(private messagesService: MessagesService, private notificationsService: NotificationsService) { }

  ngOnInit() {
    this.refreshData();
  }

  ngOnDestroy() {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
  }

  private refreshData(): void {
    // get new messages since last update
    this.messagesService.getMessages(this.type, this.lastId, MessageTableComponent.MAX_MESSAGES_TO_SHOW).subscribe(
      result => {
        // set for new messages the isNew flag
        result.forEach((message) => message.isNew = true);

        if (this.showNotifications && result.length > 0) {
          this.notificationsService.info('New Message', result.length + ' messages received', { timeOut: 2000 });
        }

        // reset this flag after some time
        setTimeout(() => result.forEach((message) => message.isNew = false),
          MessageTableComponent.NEW_MESSAGE_INDICATOR_TIME_MS);

        // put new messages at the beginn (newest messages first)
        this.messages = result.concat(this.messages);

        // truncate array to max size
        if (this.messages.length > MessageTableComponent.MAX_MESSAGES_TO_SHOW) {
          this.messages = this.messages.slice(0, MessageTableComponent.MAX_MESSAGES_TO_SHOW - 1);
        }

        // determine last id for next update
        for (const message of this.messages) {
          if (this.lastId < message.id) {
            this.lastId = message.id;
          }
        }
        this.subscribeToData();
      }
    );
  }

  private subscribeToData(): void {
    this.timerSubscription = Observable.timer(MessageTableComponent.REFRESH_INTERVAL_MS).first().subscribe(() => {
      this.refreshData();
    });
  }

  clear() {
    this.messages = [];
  }
}
