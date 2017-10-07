import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { MessageType } from './message-type';
import { SimpleMessage } from './simple-message';
import { RequestOptions, URLSearchParams, Headers } from '@angular/http';

@Injectable()
export class MessagesService {

  constructor(private http: Http) { }

  getOutgoingQueues(): Observable<Array<string>> {
    return this.http.get('api/queues')
      .map((res: Response) => res.json());
  }

  getNewMessageProperties(): Observable<Array<string>> {
    return this.http.get('api/properties')
      .map((res: Response) => res.json());
  }

  sendMessage(queue: string, message: string, count: number, props: any): Observable<Response> {
    const data: URLSearchParams = new URLSearchParams();
    data.append('count', count.toString());
    return this.http.post('api/send', { queue: queue, text: message, props: props }, { params: data });
  }

  sendFileForBulk(queue: string, file: File): Observable<number> {
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);
    formData.append('queue', queue);

    return this.http.post('api/bulkFile', formData)
      .map(res => res.json().count);
  }

  getServerWorkInProgress(): Observable<any> {
    return this.http.get('api/workInProgress')
      .map((res: Response) => res.json());
  }

  getMessages(messageType: MessageType, lastId: number, maxCount: number): Observable<Array<SimpleMessage>> {
    const data: URLSearchParams = new URLSearchParams();
    data.append('messageType', messageType.toString());
    data.append('lastId', lastId.toString());
    data.append('maxCount', maxCount.toString());

    return this.http.get('api/messages', { params: data })
      .map(this.convertToMessage);
  }

  getListenerStatus(): Observable<Map<String, Object>> {
    return this.http.get('/api/statusListener')
      .map((res: Response) => this.convertToMap(res.json()));
  }

  stopLister(queue: string): Observable<Response> {
    const data: URLSearchParams = new URLSearchParams();
    data.append('queue', queue);
    return this.http.post('api/stopListener', {}, { params: data });
  }

  startLister(queue: string): Observable<Response> {
    const data: URLSearchParams = new URLSearchParams();
    data.append('queue', queue);
    return this.http.post('api/startListener', {}, { params: data });
  }

  private convertToMessage(res: Response) {
    const data = res.json() as SimpleMessage[] || [];
    data.forEach((d) => {
      // convert string to date
      d.timestamp = new Date(d.timestamp);
    });

    return data;
  }

  private convertToMap(json): Map<String, Object> {
    let result = new Map();
    Object.keys(json).forEach(k => result.set(k, json[k]));

    return result;
  }
}
