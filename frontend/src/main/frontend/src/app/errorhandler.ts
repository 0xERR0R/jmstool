import { ErrorHandler, Injector, Injectable } from '@angular/core';
import { NotificationsService, Notification } from 'angular2-notifications';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable()
export class NotificationErrorHandler implements ErrorHandler {

  private _backendDownNotification: Notification;
  constructor(private notificationsService: NotificationsService) {
  }

  handleError(error) {
    let errorText: String;
    console.log("status" + error)
    if (error instanceof HttpErrorResponse) {
      if (error.status == 404 || error.status == 504 || error.status == 0) {
        // backend not available
        if (!this._backendDownNotification) {
          // no notification is beeing displayed, create one
          this._backendDownNotification = this.notificationsService.alert('Communication problem',
            'Backend down?', {
              animate: 'scale',
              showProgressBar: false,
              pauseOnHover: false,
              clickToClose: true
            });

          this._backendDownNotification.click.subscribe((event) => {
            this.notificationsService.remove(this._backendDownNotification.id);
            this._backendDownNotification = null;
          });
        }
        return;
      }
      let errorResp: HttpErrorResponse = error;
      errorText = errorResp.message + " (" + errorResp.error + ")"
    }
    else {
      errorText = error;
    }
    this.notificationsService.error('Error',
      'Error! is occured, more information in console log: ' + errorText, { timeOut: 0 });
    throw (error);
  }
}
