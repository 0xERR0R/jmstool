import { Component, OnInit, OnDestroy } from '@angular/core';
import { NotificationsService, Notification } from 'angular2-notifications';
import { Observable } from 'rxjs/Rx';
import { Subscription } from 'rxjs/Subscription';
import { MessagesService } from '../messages.service';

@Component({
  selector: 'work-in-progress',
  template: ''
})
export class WorkInProgressComponent implements OnInit, OnDestroy {

  /** Polling interval if no server work is expected*/
  public static readonly DEFAULT_INTERVAL_MS = 5000;

  /** Polling interval if the server is currently busy */
  public static readonly WORK_IN_PROGRESS_INTERVAL_MS = 500;

  private updateInterval = WorkInProgressComponent.DEFAULT_INTERVAL_MS;
  private timerSubscription: Subscription;

  private notification: Notification;

  constructor(private messagesService: MessagesService, private notificationsService: NotificationsService) { }

  ngOnInit() {
    this.queryServerWorkInProgress();
  }

  ngOnDestroy() {
    if (this.timerSubscription) {
      this.timerSubscription.unsubscribe();
    }
  }

  private queryServerWorkInProgress(): void {

    this.messagesService.getServerWorkInProgress()
      .finally(() =>     // make next subscription with timer
        this.timerSubscription = Observable
          .timer(this.updateInterval)
          .first()
          .subscribe(() => {
            this.queryServerWorkInProgress();
          }))
      .subscribe(t => {
        // server is busy, show notification with progress informations
        if (t.pendingCount > 0) {
          // update faster
          this.updateInterval = WorkInProgressComponent.WORK_IN_PROGRESS_INTERVAL_MS;

          // show amount of pending messages and optionally the total error count
          let notificationText: string = t.pendingCount + " pending messages" + (t.totalErrorCount > 0 ? ", total error count: " + t.totalErrorCount : "");

          // there is already a notification, uptate the text
          if (this.notification) {
            this.notification.content = notificationText;
          }
          else {
            // no notification is beeing displayed, create one
            this.notification = this.notificationsService.alert('Work in progress',
              notificationText, {
                animate: 'scale',
                showProgressBar: false,
                pauseOnHover: false,
                clickToClose: false
              });
          }
        }
        // server has no work
        else {
          // remove notification, if one is beeing displayed
          if (this.notification) {
            // polling with default interval
            this.updateInterval = WorkInProgressComponent.DEFAULT_INTERVAL_MS;
            this.notificationsService.remove(this.notification.id);
            this.notification = null;
          }
        }
      }
      );
  }

}
