import { ErrorHandler, Injector, Injectable } from '@angular/core';
import { NotificationsService } from 'angular2-notifications';

@Injectable()
export class NotificationErrorHandler implements ErrorHandler {

  constructor(private notificationsService: NotificationsService) {
  }

  handleError(error) {
    this.notificationsService.error('Error',
      'Error is occured, more information in console log: ' + error, { timeOut: 0 });
    throw (error);
  }
}
