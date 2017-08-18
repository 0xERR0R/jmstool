import { Component } from '@angular/core';
import { Http, Response, RequestOptions, Headers } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { NotificationsService } from 'angular2-notifications';
import { ViewChild } from '@angular/core';
import { MessagesService } from '../messages.service';


@Component({
  selector: 'new-file-message',
  templateUrl: './new-file-message.component.html'
})
export class NewFileMessageComponent {
  selectedQueue: string;
  selectedFile: File;
  busy: boolean = false;

  @ViewChild('fileInput')
  fileInput: any;

  constructor(private messagesService: MessagesService, private notificationsService: NotificationsService) {

  }

  sendFile() {
    if (this.selectedFile != null) {
      this.busy = true;

      this.messagesService.sendFileForBulk(this.selectedQueue, this.selectedFile)
        .finally(() => {

          this.busy = false;
          this.selectedFile = null;
          this.fileInput.nativeElement.value = "";
        })
        .subscribe(count =>
          this.notificationsService.success('Bulk send complete', count + ' messages enqueued', { timeOut: 3000 }));
    }


  }

  fileChange(fileInput) {
    let fileList: FileList = fileInput.files;
    this.selectedFile = fileList.length > 0 ? fileList[0] : null;
  }
}
