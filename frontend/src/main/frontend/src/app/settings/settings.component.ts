import { Component, ViewChild } from '@angular/core';
import { ModalDirective } from 'ngx-bootstrap';
import { MessagesService } from '../messages.service';

@Component({
  selector: 'settings',
  templateUrl: './settings.component.html'
})
export class SettingsComponent {

  @ViewChild('childModal') public childModal: ModalDirective;

  queueStatus: Array<{queue: string, running: boolean}> = [];

  constructor(private messagesService: MessagesService) {

  }

  private loadListenerStatus() {
    this.messagesService.getListenerStatus().subscribe(
      result => result.forEach((running: boolean, queue: string) => {
        const element = this.queueStatus.find(q => q.queue === queue);
        if (element) {
          element.running = running;
        }
        else {
          this.queueStatus.push({ 'queue': queue, 'running': running });
        }
      }));
  }

  public showDialog() {
    this.loadListenerStatus();
    this.childModal.show();
  }

  public hideChildModal(): void {
    this.childModal.hide();
  }

  public startListener(queue: string) {
    this.messagesService.startLister(queue).subscribe(
      result => this.loadListenerStatus()
    )
  }

  public stopListener(queue: string) {
    this.messagesService.stopLister(queue).subscribe(
      result => this.loadListenerStatus()
    )
  }
}
