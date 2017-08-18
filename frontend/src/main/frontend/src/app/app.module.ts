import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpModule } from '@angular/http';
import { ModalModule } from 'ngx-bootstrap';
import { SimpleNotificationsModule } from 'angular2-notifications';

import { AppComponent } from './app.component';
import { NewMessagePanelComponent } from './new-message-panel/new-message-panel.component';
import { NewTextMessageComponent } from './new-text-message/new-text-message.component';
import { NewFileMessageComponent } from './new-file-message/new-file-message.component';
import { MessagesService } from './messages.service';
import { MessagesComponent } from './messages/messages.component';
import { MessageTableComponent } from './message-table/message-table.component';
import { OutgoingQueuesSelectComponent } from './outgoing-queues-select/outgoing-queues-select.component';
import { AbbreviatePipe } from './abbreviate.pipe';
import { ErrorHandler } from '@angular/core';
import { NotificationErrorHandler } from './errorhandler';
import { WorkInProgressComponent } from './work-in-progress/work-in-progress.component';

@NgModule({
  declarations: [
    AppComponent,
    NewTextMessageComponent,
    NewMessagePanelComponent,
    NewFileMessageComponent,
    OutgoingQueuesSelectComponent,
    MessageTableComponent,
    MessagesComponent,
    AbbreviatePipe,
    WorkInProgressComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    ModalModule.forRoot(),
    SimpleNotificationsModule.forRoot(),
    BrowserAnimationsModule
  ],
  providers: [MessagesService, {provide: ErrorHandler, useClass: NotificationErrorHandler}],
  bootstrap: [AppComponent]
})
export class AppModule { }
