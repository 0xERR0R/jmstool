import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { HttpClient, HttpParams} from '@angular/common/http';
import { Observable } from 'rxjs/Rx';
import { MessageType } from './message-type';
import { SimpleMessage } from './simple-message';
import { RequestOptions, URLSearchParams, Headers } from '@angular/http';
import { MessageResult } from './message-result';

@Injectable()
export class MessagesService {

  constructor(private _http: HttpClient) { }

  getOutgoingQueues(): Observable<Array<string>> {
    return this._http.get<Array<string>>('api/queues');
  }

  getNewMessageProperties(): Observable<Array<string>> {
    return this._http.get<Array<string>>('api/properties');
  }

  sendMessage(queue: string, message: string, count: number, props: any): Observable<any> {
    let params = new HttpParams()
                 .set('count', count.toString());
    return this._http.post('api/send', { queue: queue, text: message, props: props }, { params });
  }

  sendFileForBulk(queue: string, file: File): Observable<number> {
    const formData: FormData = new FormData();
    formData.append('file', file, file.name);
    formData.append('queue', queue);

    return this._http.post<number>('api/bulkFile', formData);
  }

  getServerWorkInProgress(): Observable<any> {
    return this._http.get('api/workInProgress');
  }

  getMessages(messageType: MessageType, lastId: number, maxCount: number): Observable<MessageResult> {
    let params = new HttpParams()
                 .set('messageType', messageType.toString())
                 .set('lastId', lastId.toString())
                 .set('maxCount', maxCount.toString());

    return this._http.get('api/messages', { params })
      .map(this.convertToMessageResult);
  }

  getListenerStatus(): Observable<Map<String, Object>> {
    return this._http.get('api/statusListener')
      .map((res: Response) => this.convertToMap(res));
  }

  stopLister(queue: string): Observable<any> {
    let params = new HttpParams()
                 .set('queue', queue);
    return this._http.post('api/stopListener', {}, { params });
  }

  startLister(queue: string): Observable<any> {
    let params = new HttpParams()
                 .set('queue', queue);
    return this._http.post('api/startListener', {}, { params });
  }

  private convertToMessageResult(res: any): MessageResult {
    const data = res.messages as SimpleMessage[] || [];
    data.forEach((d) => {
      // convert string to date
      d.timestamp = new Date(d.timestamp);
    });

    let result: MessageResult = { messages: data, lastId: res.lastId}
    return result;
  }

  private convertToMap(json): Map<String, Object> {
    const result = new Map();
    Object.keys(json).forEach(k => result.set(k, json[k]));

    return result;
  }
}
