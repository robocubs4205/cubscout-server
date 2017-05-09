import {Injectable} from '@angular/core';
import {Result, ResultSubmission} from "./result";
import {Observable} from "rxjs";
import {Match} from "./match";
import {Link} from "./link";
import {Http, RequestOptions, Headers, Response, URLSearchParams} from "@angular/http";
import {Event} from "./event"
import {Scorecard} from "./scorecard";

@Injectable()
export class ResultService {

  submitResult(result: ResultSubmission, match: Match): Observable<Result> {
    let link: Link = match.links.find(link => link.rel == "results");
    if (link == null) return Observable.throw("game does not have link with rel \"results\"");
    return this.http.post(link.href,
                          result,
                          new RequestOptions({
                            headers: new Headers([
                              {'accept': 'application/vnd.robocubs-v1+json'}
                            ])
                          })).map(res => res.json()).catch(error => {
      let errMsg: string;
      if (error instanceof Response) {
        const body = error.json() || '';
        const err = body.error || JSON.stringify(body);
        errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
        errMsg += JSON.stringify(error);
      } else {
        errMsg = error.message ? error.message : error.toString();
      }
      console.error("failed to submit result");
      console.error(errMsg);
      return Observable.throw(errMsg);
    });
  }

  getResults(event: Event, scorecard: Scorecard): Observable<Result[]> {
    let link: Link = event.links.find(link => link.rel == "results");
    if (link == null) return Observable.throw("event does not have link with rel \"results\"");
    let params: URLSearchParams = new URLSearchParams();
    params.set("scorecard", scorecard.id.toString());
    console.log("getting results");
    return this.http.get(link.href, new RequestOptions({
      headers: new Headers([
        {'accept': 'application/vnd.robocubs-v1+json'}
      ]),
      search: params
    })).map(res => res.json().data).catch(error => {
        let errMsg: string;
        if (error instanceof Response) {
          const body = error.json() || '';
          const err = body.error || JSON.stringify(body);
          errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
          errMsg += JSON.stringify(error);
        } else {
          errMsg = error.message ? error.message : error.toString();
        }
        console.error("failed to get results");
        console.error(errMsg);
        return Observable.throw(errMsg);
      });
  }

  constructor(private http: Http) { }

}


