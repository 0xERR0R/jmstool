import * as vkbeautify from 'vkbeautify';
import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
    name: 'beautifyXml'
})
export class BeautifyXmlPipe implements PipeTransform {
    transform(value: string): string {
        return vkbeautify.xml(value);
    }
}
